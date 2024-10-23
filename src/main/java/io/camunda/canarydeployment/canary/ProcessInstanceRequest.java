package io.camunda.canarydeployment.canary;

import java.util.Map;

/**
 * JSON:
 *
 * {
 *   "processDefinitionKey": 0,
 *   "bpmnProcessId": "string",
 *   "version": -1,
 *   "variables": {},
 *   "tenantId": "string",
 *   "operationReference": 0,
 *   "startInstructions": [
 *     {
 *       "elementId": "string"
 *     }
 *   ],
 *   "awaitCompletion": false,
 *   "requestTimeout": 0
 * }
 */
public class ProcessInstanceRequest {

  public String bpmnProcessId;
  public Map<String,Object> variables;
  public String versionTag;
  public int version;

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public String getVersionTag() {
    return versionTag;
  }

  public void setVersionTag(String versionTag) {
    this.versionTag = versionTag;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
}
