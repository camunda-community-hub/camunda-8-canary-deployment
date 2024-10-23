
[![Community badge: Stable](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)

# Canary Deployment

A Canary Deployment consists of deploying a component and sending part of the traffic to this component. If any failure is detected, the original platform must be set up.

This project demonstrates how to do that dynamically with Camunda 8 without stopping the server. It demonstrates the feature on two different artifacts
* on a service task. Deploy a new version of a service task, and send 20 % of traffic to this new service task
* on a process. Deploy a new version of a process and send 15 % of traffic to this new process.

Both do not need to stop the server or change the application.


# Different function
This project builds an application. This application contains multiple functions to demonstrate different steps.

A Kubernetes folder (k8) contains all the different Kubernetes files needed to start the component. This is the same image but with a different configuration.

This is why different Kubernetes deployments are present in the k8 folder: they can start one pod running only one function.

## canaryDeployment
This application is a load balancer between version processes. It offers the same API as the REST Zeebe Gateway, but rules can be onboard to load balance traffic between different versions of the same process, per percentage.


## customer application Ruby
The Ruby application simulates a customer application, which creates process instances.

## Workers
The workers define different workers

# Build

Rebuilt the image via
````
mvn clean install
docker build -t pierre-yves-monnet/canarydeployment:1.0.0 .
docker tag pierre-yves-monnet/canarydeployment:1.0.0 ghcr.io/camunda-community-hub/canarydeployment:1.0.0
docker push ghcr.io/camunda-community-hub/canarydeployment:1.0.0

docker tag pierre-yves-monnet/canarydeployment:1.0.0 ghcr.io/camunda-community-hub/canarydeployment:latest
docker push ghcr.io/camunda-community-hub/canarydeployment:latest


````

The docker image is built using the Dockerfile present on the root level.


Push the image to
```
ghcr.io/camunda-community-hub/process-execution-automator:
```


# Scenario
This section explains how to demonstrate a canary deployment step-by-step

## Initial platform
For all scenarios, a Camunda 8.6 platform is up and running. The values.yaml used is
```yaml

global:
  identity:
    auth:
      enabled: false

identity:
  enabled: false

identityPostgresql:
  enabled: false

prometheusServiceMonitor:
  enabled: true
```

A Grafana page is started and is accessible.
```shell
kubectl get svc -n default
CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
kubernetes                              ClusterIP      34.118.224.1     <none>        443/TCP             53d
metrics-grafana                         ClusterIP      34.118.239.223   <none>        80/TCP              53d
metrics-grafana-loadbalancer            LoadBalancer   34.118.226.41    34.23.97.79   80:32264/TCP        53d
```

Check the external IP and start the browser on it (http://34.23.97.79)

Visit https://github.com/camunda-community-hub/camunda-8-helm-profiles/blob/3c54b0f33191b65de6451c4675e23cd00366e2a9/metrics

# Service Task Canary
Access the procedure
[README.md](doc/CanaryServiceTask/README.md)

# Process Canary
Access the procedure
[README.md](doc/CanaryProcess/README.md)