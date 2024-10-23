package io.camunda.canarydeployment.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties()
@Component
@RestController

public class WorkerApplication {

  public static final String WORKER_GET_PRICE = "getPrice";
  public static final String WORKER_GET_PRICE_V2 = "getPriceV2";
  Logger logger = LoggerFactory.getLogger(WorkerApplication.class.getName());

  @Autowired
  ZeebeClient zeebeClient;

  @Value("${canary.workerApplication.enabled:true}")
  public Boolean workerApplicationEnabled;

  @Value("${canary.workerApplication.getPrice:false}")
  public Boolean workerGetPriceEnabled;

  @Value("${canary.workerApplication.getPriceV2:false}")
  public Boolean workerGetPriceV2Enabled;

  @PostConstruct
  public void init() {
    logger.info("Init WorkerApplication - enabled[{}]", workerApplicationEnabled);

    manageWorkers();
  }

  /**
   * Manage workers: open or close it according the configuration
   */
  private void manageWorkers() {
    if (!Boolean.TRUE.equals(workerApplicationEnabled)) {
      closeWorker(WORKER_GET_PRICE);
      closeWorker(WORKER_GET_PRICE_V2);

      return;
    }

    if (workerGetPriceEnabled) {
      openWorker(WORKER_GET_PRICE, "get-price",0);
    } else {
      closeWorker(WORKER_GET_PRICE);
    }

    if (workerGetPriceV2Enabled) {
      openWorker(WORKER_GET_PRICE_V2, "get-price-v2", 80);
    } else {
      closeWorker(WORKER_GET_PRICE_V2);
    }
  }


  private final Map<String, JobWorker> workersMap = new HashMap<>();

  /**
   * Open a woker
   * @param workerName name of worker
   * @param jobType type of worker
   * @param basicValue basic value to give to the worker
   */
  private void openWorker(String workerName, String jobType, int basicValue) {
    try {
      JobHandler getPriceWorker = new GetpriceWorker(workerName, basicValue);
      JobWorker jobWorker = zeebeClient.newWorker().jobType(jobType).handler(getPriceWorker).streamEnabled(true).open();
      workersMap.put(workerName, jobWorker);
      logger.info("Worker[{}] type[{}] started", workerName, jobType);
    }
    catch(Exception e){
      logger.error("Error during start Worker[{}] type[{}] : {} ", workerName, jobType, e);
    }
  }

  /**
   * Close a worker
   * @param workerName
   */
  private void closeWorker( String workerName) {
    JobWorker jobWorker = workersMap.get(workerName);
  if (jobWorker!=null && jobWorker.isOpen())
    jobWorker.close();
  }

  @GetMapping("/worker/status")
  public String getStatus() {
    String status = "";
    if (workerGetPriceEnabled) {
      JobWorker jobWorker = workersMap.get(WORKER_GET_PRICE);
      status += "WorkerGetPrice-open(" + (jobWorker != null && jobWorker.isOpen() ? "true" : "false") + ")";
    }
    if (workerGetPriceV2Enabled) {
      JobWorker jobWorker = workersMap.get(WORKER_GET_PRICE_V2);
      status += "WorkerGetPriceV2-open(" + (jobWorker != null && jobWorker.isOpen() ? "true" : "false") + ")";
    }

    return status;
  }
}
