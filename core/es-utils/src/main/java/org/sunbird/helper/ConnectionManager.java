/** */
package org.sunbird.helper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;

/**
 * This class will manage connection.
 *
 * @author Manzarul
 */
public class ConnectionManager {
  private static final LoggerUtil logger = new LoggerUtil(ConnectionManager.class);

  private static RestHighLevelClient restClient = null;
  private static List<String> host = new ArrayList<>();
  private static List<Integer> ports = new ArrayList<>();

  static {
    System.setProperty("es.set.netty.runtime.available.processors", "false");
    initialiseRestClientConnection();
    registerShutDownHook();
  }

  private ConnectionManager() {}

  private static boolean initialiseRestClientConnection() {
    boolean response = false;
    try {
      String cluster = System.getenv(JsonKey.SUNBIRD_ES_CLUSTER);
      String hostName = System.getenv(JsonKey.SUNBIRD_ES_IP);
      String port = System.getenv(JsonKey.SUNBIRD_ES_PORT);
      if (StringUtils.isBlank(hostName) || StringUtils.isBlank(port)) {
        return false;
      }
      String[] splitedHost = hostName.split(",");
      for (String val : splitedHost) {
        host.add(val);
      }
      String[] splitedPort = port.split(",");
      for (String val : splitedPort) {
        ports.add(Integer.parseInt(val));
      }
      response = createRestClient(cluster, host);
      logger.info(
          "ELASTIC SEARCH CONNECTION ESTABLISHED for restClient from EVN with Following Details cluster "
              + cluster
              + "  hostName"
              + hostName
              + " port "
              + port
              + response);
    } catch (Exception e) {
      logger.error("Error while initialising connection for restClient from the Env", e);
      return false;
    }
    return response;
  }

  /**
   * This method will provide ES transport client.
   *
   * @return TransportClient
   */
  public static RestHighLevelClient getRestClient() {
    if (restClient == null) {
      logger.info("ConnectionManager:getRestClient eLastic search rest clinet is null ");
      initialiseRestClientConnection();
      logger.info(
          "ConnectionManager:getRestClient after calling initialiseRestClientConnection ES client value ");
    }
    return restClient;
  }

  /**
   * This method will create the client instance for elastic search.
   *
   * @param clusterName String
   * @param host List<String>
   * @return boolean
   * @throws UnknownHostException
   */
  private static boolean createRestClient(String clusterName, List<String> host) {
    HttpHost[] httpHost = new HttpHost[host.size()];
    for (int i = 0; i < host.size(); i++) {
      httpHost[i] = new HttpHost(host.get(i), 9200);
    }
    restClient = new RestHighLevelClient(RestClient.builder(httpHost));
    logger.info("ConnectionManager:createRestClient client initialisation done. ");
    return true;
  }

  /**
   * This class will be called by registerShutDownHook to register the call inside jvm , when jvm
   * terminate it will call the run method to clean up the resource.
   *
   * @author Manzarul
   */
  public static class ResourceCleanUp extends Thread {
    @Override
    public void run() {
      try {
        if (null != restClient) {
          restClient.close();
        }
      } catch (IOException e) {
        logger.info(
            "ConnectionManager:ResourceCleanUp error occured during restclient resource cleanup "
                + e);
      }
    }
  }

  /** Register the hook for resource clean up. this will be called when jvm shut down. */
  public static void registerShutDownHook() {
    Runtime runtime = Runtime.getRuntime();
    runtime.addShutdownHook(new ResourceCleanUp());
  }
}
