package io.camunda.canarydeployment.canary;

class RuleCondition {
  int percent;
  String versionTag;
  Integer version;

  public int getPercent() {
    return percent;
  }

  public void setPercent(int percent) {
    this.percent = percent;
  }

  public String getVersionTag() {
    return versionTag;
  }

  public void setVersionTag(String versionTag) {
    this.versionTag = versionTag;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}
