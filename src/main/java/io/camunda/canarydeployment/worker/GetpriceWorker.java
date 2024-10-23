package io.camunda.canarydeployment.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GetpriceWorker implements JobHandler {
  public static final String PROCESS_VARIABLE_CALCULATION = "calculation";
  public static final String PROCESS_VARIABLE_PRICELABEL = "priceLabel";
  public static final String PROCESS_VARIABLE_PLEASELOG = "pleaselog";
  public static final String PROCESS_VARIABLE_REVIEW="review";
  public static final String PROCESS_VARIABLE_DELAYMS = "delayms";
  Logger logger = LoggerFactory.getLogger(GetpriceWorker.class.getName());

  Random random = new Random();

  private String label;
  private int basicValue;
  public GetpriceWorker(String label, int basicValue) {
    this.label=label;
    this.basicValue= basicValue;
  }

  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
       logger.info("GetPriceWorker [{}]", label);


    Map<String, Object> variables = new HashMap<>();
    variables.put(PROCESS_VARIABLE_CALCULATION, basicValue);
    variables.put(PROCESS_VARIABLE_PRICELABEL, label);
    variables.put(PROCESS_VARIABLE_REVIEW, random.nextInt(100)+basicValue);
    jobClient.newCompleteCommand(job.getKey()).variables(variables).send();
  }

}
