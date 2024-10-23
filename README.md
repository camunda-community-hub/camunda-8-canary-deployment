# canarydeployment

This application contains multiple application, which can start individualy

# Different function 
The application can start functions. The function is piloted by a configuration variable.
This is why in the k8 folder, different kubernetes deployment are present: they can start one pod running only one function.

## canaryDeployment

## customerApplication Ruby

## Workers

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

The docker image is build using the Dockerfile present on the root level.


Push the image to
```
ghcr.io/camunda-community-hub/process-execution-automator:
```


# Scenario
This section explain how to demonstrate a canary deployment, step by step

## Initial platform
For all scenario, a Camunda 8.6 platform is up and running. The values.yaml used is
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

A Grafana page is started and are accessible.
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
