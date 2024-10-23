package io.camunda.canarydeployment.canary;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.Set;

@ConfigurationProperties()
@Component

@RestController
public class CanaryApplication {
  Logger logger = LoggerFactory.getLogger(CanaryApplication.class.getName());

  @Autowired
  ZeebeClient zeebeClient;

  @Autowired
  RuleFactory ruleFactory;

  @Value("${canary.canaryapplication.enabled:true}")
  public Boolean canaryApplicationEnabled;

  Random random = new Random();

  @PostConstruct
  public void init() {
    logger.info("InitCanaryApplication - enabled[{}]", canaryApplicationEnabled);
  }

  /* ******************************************************************** */
  /*                                                                      */
  /*  Create process instance                                             */
  /*                                                                      */
  /* ******************************************************************** */

  @PostMapping("/v2/process-instances")
  public String createProcessInstance(@RequestBody ProcessInstanceRequest processInstanceRequest) {
    if (!Boolean.TRUE.equals(canaryApplicationEnabled))
      throw new RuntimeException("Canary application is not enabled");

    try {
      Rule rule = ruleFactory.getByProcessId(processInstanceRequest.bpmnProcessId);
      if (rule != null) {
        RuleCondition ruleCondition = chooseRandomCondition(rule);
        if (ruleCondition != null) {
          processInstanceRequest.versionTag = ruleCondition.versionTag;
          processInstanceRequest.version = ruleCondition.version;
          if (ruleCondition.version == null) {
            logger.info("Version is not provided in the rule, so detect it");
            ruleCondition.version = ruleFactory.getVersionFromVersionTag(processInstanceRequest.bpmnProcessId,
                ruleCondition.versionTag);
          }
        }
      }

      CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep2 step2 = zeebeClient.newCreateInstanceCommand()
          .bpmnProcessId(processInstanceRequest.bpmnProcessId);

      CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3 step3;
      boolean useLatest =processInstanceRequest.version == 0 || processInstanceRequest.version == -1;
      if (useLatest)
        step3 = step2.latestVersion();
      else
        step3 = step2.version(processInstanceRequest.version);

      ProcessInstanceEvent processInstance = step3.variables(processInstanceRequest.variables).send().join();
      String pid = String.valueOf(processInstance.getProcessInstanceKey());
      logger.info("Process-instances created in processId[{}] Version[{}] PID[{}]",
          processInstanceRequest.bpmnProcessId, //
          useLatest? "latest": processInstanceRequest.version,
          pid);
      return pid;
    } catch (Exception e) {
      logger.error("During creation ProcessInstance in processId[{}] Version[{}] :: {}",
          processInstanceRequest.bpmnProcessId, processInstanceRequest.version, e);
      throw new RuntimeException("Canary application Can't create process instance");
    }
  }

  private RuleCondition chooseRandomCondition(Rule rule) {
    int chance = random.nextInt(100);
    int sum = 0;
    List<RuleCondition> listConditions = rule.getConditions();
    if (listConditions.isEmpty())
      return null;
    for (int i = 0; i < listConditions.size(); i++) {
      if (chance <= sum + listConditions.get(i).percent)
        return listConditions.get(i);
      sum += listConditions.get(i).percent;
    }
    return listConditions.get(listConditions.size() - 1);
  }

  /* ******************************************************************** */
  /*                                                                      */
  /*  Rule management                                                     */
  /*                                                                      */
  /* ******************************************************************** */

  @GetMapping("/canary/rules")
  public Set<Rule> getRules() {
    if (!Boolean.TRUE.equals(canaryApplicationEnabled))
      throw new RuntimeException("Canary application is not enabled");

    logger.info("GetRules");
    return ruleFactory.getRules();

  }

  @PostMapping("/canary/addrule")
  public String addRules(@RequestBody Rule rule) {
    if (!Boolean.TRUE.equals(canaryApplicationEnabled))
      throw new RuntimeException("Canary application is not enabled");

    logger.info("AddRule processId[{}] ", rule.processId);

    String status = ruleFactory.setRule(rule);
    return "Rule added? " + status + " Number of rules : " + ruleFactory.getRules().size();

  }

  @PostMapping("/canary/deleterule/{processId}")
  public String addRules(@PathVariable String processId) {
    if (!Boolean.TRUE.equals(canaryApplicationEnabled))
      throw new RuntimeException("Canary application is not enabled");

    logger.info("DeleteRule processId[{}] ", processId);
    ruleFactory.removeRule(processId);
    return "Rule Deleted. Number of rules : " + ruleFactory.getRules().size();
  }

}
