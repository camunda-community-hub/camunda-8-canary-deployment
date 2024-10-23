package io.camunda.canarydeployment.canary;

import java.util.ArrayList;
import java.util.List;

class Rule {
  String processId;
  List<RuleCondition> conditions = new ArrayList<>();


  public String getProcessId() {
    return processId;
  }

  public List<RuleCondition> getConditions() {
    return conditions;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public void setConditions(List<RuleCondition> conditions) {
    this.conditions = conditions;
  }
}
