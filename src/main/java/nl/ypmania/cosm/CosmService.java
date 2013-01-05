package nl.ypmania.cosm;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

@Component
public class CosmService {
  
  private static final Logger log = LoggerFactory.getLogger(CosmService.class);
  private String apiKey = System.getProperty("cosm.apikey");
  private int feed = 94446;
  private Client client;
  
  @PostConstruct
  public void init() {
    log.debug("Initing");
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = Client.create(clientConfig);
    client.setConnectTimeout(5000);
    client.setReadTimeout(5000);
    log.debug("Inited");
  }
  
  public void bryggers(double value) {
    System.out.println("Bruggers to " + value);
    log.warn("Updating bryggers to {}", value);
    updateDatapoint("Bryggers", value);
  }
  
  private void updateDatapoint (String datastream, double value) {
    log.debug("Updating {} to {}", datastream, value);
    WebResource resource = client.resource("https://api.cosm.com/v2/feeds/" + feed + "/datastreams/" + datastream);
    String result = resource.header("X-ApiKey", apiKey).entity("{\"current_value\": \"" + value + "\"}", "application/json").put(String.class);
    log.debug(result);
  }

}