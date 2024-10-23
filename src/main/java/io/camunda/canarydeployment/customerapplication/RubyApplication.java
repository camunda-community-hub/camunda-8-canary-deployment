package io.camunda.canarydeployment.customerapplication;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.annotation.PostConstruct;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Component
@ConfigurationProperties()
@RestController

public class RubyApplication {
  Logger logger = LoggerFactory.getLogger(RubyApplication.class.getName());

  @Autowired
  ZeebeClient zeebeClient;

  @Value("${canary.rubyApplication.enabled:false}")
  public Boolean rubyApplicationEnabled;

  @Value("${canary.rubyApplication.interval:10000}")
  public Long interval;

  @Value("${canary.rubyApplication.processId:}")
  public String processId;

  @Value("${canary.rubyApplication.zeebeClient:}")
  public Boolean useZeebeClient;

  @Value("${canary.rubyApplication.numberOfPi:}")
  public Integer numberOfPi;

  @Value("${canary.rubyApplication.restGateway:}")
  public String restGateway;

  @Autowired
  private TaskScheduler taskScheduler;

  @PostConstruct
  public void init() {
    logger.info("InitRubyApplication - enabled[{}]", rubyApplicationEnabled);

    if (rubyApplicationEnabled) {
      scheduleHearth();
    }

  }




  /* ******************************************************************** */
  /*                                                                      */
  /*  Administration                                                      */
  /*                                                                      */
  /* ******************************************************************** */

  @PutMapping("/ruby/update-interval/{interval}")
  public String updateInterval(@PathVariable long interval) {
    this.interval = interval;
    return "interval updated to " + interval + " ms";
  }

  @PostMapping("/ruby/stop")
  public String stopTask() {
    rubyApplicationEnabled = false;
    return "Customer application stopped.";
  }

  @PostMapping("/ruby/start")
  public String startTask() {
    rubyApplicationEnabled = true;
    scheduleHearth();
    return "Customer Application started.";
  }

  @GetMapping("/ruby/status")
  public String getStatus() {
    return "Ruby is started: " + rubyApplicationEnabled;
  }

  /* ******************************************************************** */
  /*                                                                      */
  /*  Hearth                                                      */
  /*                                                                      */
  /* ******************************************************************** */

  private void scheduleHearth() {
    if (rubyApplicationEnabled) {
      if (interval < 1000) {
        logger.info("Interval too low (min 1s), set it to 10s");

        interval = 10000L;
      }
      logger.info("Schedule a task in {} ms", interval);
      taskScheduler.schedule(this::runTask, new java.util.Date(System.currentTimeMillis() + interval));
    }
  }

  private void runTask() {
    logger.info("CustomerApplication - create {} PI", numberOfPi);
    for (int i = 0; i < numberOfPi; i++) {

      if (useZeebeClient) {
        createViaZeebeClient();
      } else
        createViaRestTemplate();
    }
    scheduleHearth();
  }

  /**
   * Return a processInstance, created via the ZeebeClient interface
   * @return
   */
  private ProcessInstanceEvent createViaZeebeClient() {
    return zeebeClient.newCreateInstanceCommand()
        .bpmnProcessId(processId)
        .latestVersion()
        .variables(new HashMap<>())
        .send()
        .join();

  }

  /**
   * Call via the REST Client
   */
  private void createViaRestTemplate() {
    String jsonInput = "{\"bpmnProcessId\": \"CustomerTickets\"," //
        + "  \"variables\" : {\"color\": \"blue\", \"weather\": \"yellow\" }}";
    try {

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");
      headers.setContentType(MediaType.APPLICATION_JSON);

      // Create HttpEntity with the JSON and headers
      HttpEntity<String> request = new HttpEntity<>(jsonInput, headers);

      // Call the REST API using RestTemplate
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response = restTemplate.exchange(restGateway, HttpMethod.POST, request, String.class);

      // Print response status code
      logger.info(" Response Code: {} on Url[{}] ",response.getStatusCodeValue(), restGateway);
    } catch (HttpClientErrorException e) {
      logger.error("HttpClientErrorException Can't send HTTP Url [{}] : {}", restGateway, e.getMessage());
    } catch (Exception e) {
      logger.error("can't send HTTP Url [{}] : {}", restGateway, e.getMessage());
    }
  }
}
