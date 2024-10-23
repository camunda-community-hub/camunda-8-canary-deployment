package io.camunda.canarydeployment.canary;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RuleFactory {

  @PostConstruct
  private void init() {
    // This is the moment to read all rules from database
  }

  private Map<String, Rule> mapRules = new HashMap<>();

  public String setRule(Rule rule) {
    if (rule==null)
      return "Give a correct rule";
    if (rule.processId==null || rule.processId.isEmpty())
      return "Rule must have a processId";
    if (rule.conditions.isEmpty())
      return "Rule must have conditions";
    if (mapRules.containsKey(rule.processId)) {
      mapRules.put(rule.processId, rule);
      return "Rule already exists, overrided";
    }

    mapRules.put(rule.processId, rule);
    return "New rule added";
  }

  public void removeRule(String processId) {
    mapRules.remove(processId);
  }

  public Set<Rule> getRules() {
    return mapRules.values().stream().collect(Collectors.toSet());
  }

  public Rule getByProcessId(String processId) {
    return mapRules.get(processId);
  }

  Map<String,Map<String,Integer>> mapProcessVersionTag;

  public int getVersionFromVersionTag(String bpmnProcessId, String versionTag) {
    Map<String, Integer> mapVersionTags = mapProcessVersionTag.get(bpmnProcessId);
    if (mapVersionTags==null) {
      mapVersionTags = new HashMap<>();
      mapProcessVersionTag.put(bpmnProcessId, mapVersionTags);
    }
    if (mapVersionTags.get(versionTag)==null) {
      int version = loadVersionFromVersionTag(bpmnProcessId, versionTag);
      mapVersionTags.put(versionTag, version);
      return version;
    }
    return mapVersionTags.get(versionTag);
  }


  private int loadVersionFromVersionTag(String bpmnProcessId, String versionTag) {
    // who ask??? There is no interface available in 8.7, except asking the XML and dig into
    return 12;
  }
}
