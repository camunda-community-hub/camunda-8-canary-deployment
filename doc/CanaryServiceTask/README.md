# Service task Canary demonstration

# Check the initial platform.
A Camunda 8 is started, and the Grafana page is accessible.

# Principle
A running process, `PurchaseRequest` exists. This process uses a service task, `get-price`.

A new version of the service task is deployed. Initially, 20 % of the traffic is sent to the new implementation. But then 80% of the traffic.
We want to return to the original version at one moment, assuming there is something to change in the implementation.
Then, 80% of the traffic is sent again, and finally, all the traffic is sent to the V2.

During this deployment, we want to verify that the server never stops and continues to accept all process instances. This will be checked via the Grafana page, metrics "start process instance."

# Preparation
1. Deploy the process `test/resources/ServiceTaskProcess/PurchaseRequest V1.bpmn`, from the modeler.
   ![PurchaseRequest-V1.png](/doc/servicetask/PurchaseRequest-V1.png)


2. start the worker application.
   The application takes in parameter the worker to start, and this deployment starts the `get-price` V1
```shell
kubectl create -f k8/servicetask/GetPriceV1.yaml
```
3. Start the application Ruby, which creates process instances on PurchaseRequest. The application takes into parameter the processId, the frequency, and the number of process instances to create
```shell
kubectk create -f k8/CanaryServiceTask/GetPriceV1.yaml
```
Check the pod starts correctly.

```shell
kubectl logs -f  <PodNameFor getpricev1>
2024-10-22T22:07:25.465Z  INFO 1 --- [           main] i.c.c.worker.WorkerApplication           : Init WorkerApplication - enabled[true]
```

4. Start the Ruby application

```shell
kubectl create -f k8/CanaryServiceTask/RubyCustomerTickets.yaml
```

Check the pod starts correctly.

```shell
kubectl logs -f  <PodNameF rubypurchaserequest>
2024-10-22T22:15:57.065Z  INFO 1 --- [           main] i.c.c.c.CustomerApplication              : InitRubyApplication - enabled[true]
2024-10-22T22:15:57.066Z  INFO 1 --- [           main] i.c.c.c.CustomerApplication              : Schedule a task in 12000 ms
```

Verify via Operate process instance is created
![OperateGetPriceV1.png](/doc/CanaryServiceTask/OperateGetPriceV1.png)

Check with Grafana for the throughput
![GrafanaGetPriceV1.png](/doc/CanaryServiceTask/GrafanaGetPriceV1.png)


# Scenario

## Deploy GetPrice V2 and send 20% of work to the new worker

Something needs to be fixed with the service task. So, a new version is produced.
The idea is to deploy this new version and send 20% of traffic to this version.

The topic for the new worker is `get-price-v2`

The second worker is started. The first one is still active.

```shell
kubectl create -f k8/CanaryServiceTask/GetPriceV2.yaml 
```

Check the pod

```shell
kubectl logs -f  <PodNameF rubypurchaserequest>
2024-10-22T22:34:21.057Z  INFO 1 --- [           main] i.c.c.canary.CanaryApplication           : InitCanaryApplication - enabled[false]
2024-10-22T22:34:21.161Z  INFO 1 --- [           main] i.c.c.c.RubyApplication                  : InitRubyApplication - enabled[false]
2024-10-22T22:34:21.164Z  INFO 1 --- [           main] i.c.c.worker.WorkerApplication           : Init WorkerApplication - enabled[true]
2024-10-22T22:34:21.472Z  INFO 1 --- [           main] i.c.c.worker.WorkerApplication           : Worker[getPriceV2] type[get-price-v2] started
````

At this moment, the second worker does not capture anything.
To send 20% of traffic to this worker, a new process is deployed `test/resources/ServiceTaskProcess/PurchaseRequest-V2-20percent.bpmn`

This process has a condition for switching between workers
![PurchaseRequestV2Detail.png](/doc/CanaryServiceTask/PurchaseRequestV2Detail.png)

Immediately, you will see some activation in the logs.
````
2024-10-22T22:34:26.166Z  INFO 1 --- [           main] i.c.canarydeployment.MainApplication     : Started MainApplication in 25.703 seconds (process running for 29.296)
2024-10-22T22:39:20.760Z  INFO 1 --- [pool-3-thread-1] i.c.c.worker.GetpriceWorker              : GetPriceWorker [getPriceV2]
2024-10-22T22:39:20.867Z  INFO 1 --- [pool-3-thread-1] i.c.c.worker.GetpriceWorker              : GetPriceWorker [getPriceV2]
2024-10-22T22:39:20.869Z  INFO 1 --- [pool-3-thread-1] i.c.c.worker.GetpriceWorker              : GetPriceWorker [getPriceV2]
2024-10-22T22:39:21.262Z  INFO 1 --- [pool-3-thread-1] i.c.c.worker.GetpriceWorker              : GetPriceWorker [getPriceV2]
2024-10-22T22:39:21.342Z  INFO 1 --- [pool-3-thread-1] i.c.c.worker.GetpriceWorker              : GetPriceWorker [getPriceV2]
2024-10-22T22:39:21.362Z  INFO 1 --- [pool-3-thread-1] i.c.c.worker.GetpriceWorker              : GetPriceWorker [getPriceV2]
````

Check the Grafana page: there is no disruption in terms of job creation per second.

![GrafanaGetPriceV2-20-percent.png](doc/CanaryServiceTask/GrafanaGetPriceV2-20-percent.png)

Operate shows that some process instances have moved to the second review task.
![GrafanaGetPriceV2-20-percent.png](doc/CanaryServiceTask/GrafanaGetPriceV2-20-percent.png)

The get-price-V2 produces data in both paths.


## Move to 80 %
To move 80 % of process instances to the new worker, deploy version 3
![PurchaseRequestV3Detail.png](/doc/CanaryServiceTask/PurchaseRequestV3Detail.png)

Check the Grafana page: there is no disruption of service. Check Operate: the repetition between Review and Review refused is better.
![OperateGetPriceV2-80-percent.png](/doc/CanaryServiceTask/OperateGetPriceV2-80-percent.png)


## Move back to the first version
It's possible to move back to the first version. Deploy the process V1 again

Version 4 is created, and again, all process instances move to the refused.
It's possible to stop the V2

```shell
kubectl delete -f k8/CanaryServiceTask/GetPriceV2.yaml 
```
![OperateGetPriceV1-BackToFirstVersion.png](/doc/CanaryServiceTask/OperateGetPriceV1-BackToFirstVersion.png)

Check the Grafana page: there is no impact on Job completion per second after the stop because this worker is not used.

## Move to the new version

Restart the GetPriceV2
```shell
kubectl create -f k8/CanaryServiceTask/GetPriceV2.yaml 
```

When the application is started, deploy the process.
This process uses only the `get-price-v2` topic.

Stop the worker version 1
```shell
kubectl delete -f k8/CanaryServiceTask/GetPriceV1.yaml 
```
Check there is no disruption on the Grafana page.

## Stop all
```shell
kubectl delete -f k8/CanaryServiceTask/GetPriceV1.yaml 
kubectl delete -f k8/CanaryServiceTask/GetPriceV2.yaml
kubectl delete -f k8/CanaryServiceTask/RubyCustomerTickets.yaml
```

# Conclusion
Via this procedure, deploying a new service task does not impact the throughput. It's easy to move the Canary percentage, return to the original situation, or ultimately deploy the new version.

The canary is based on a random value, but it can be based on other criteria, such as process variables or a DMN table.

