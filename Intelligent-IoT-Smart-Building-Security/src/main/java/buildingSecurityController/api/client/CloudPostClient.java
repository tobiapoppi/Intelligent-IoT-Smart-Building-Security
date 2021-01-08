package buildingSecurityController.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SenMLPack;

public class CloudPostClient {

    private static final Logger logger = LoggerFactory.getLogger(CloudPostClient.class);
    private String targetCloudUrl = "http://192.168.1.107:8090/cloudCollector/pack";
    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;

    public CloudPostClient(){
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.custom().build();
    }

    public CloseableHttpResponse postRequestToCloud(SenMLPack senMLPack){
        try{
            logger.info("Target Url: {}", targetCloudUrl);

            String jsonBody = this.objectMapper.writeValueAsString(senMLPack);

            //Create the HTTP Post Request
            HttpPost createPackRequest = new HttpPost(targetCloudUrl);

            //Add Content Type Header
            createPackRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            //Set Payload
            createPackRequest.setEntity(new StringEntity(jsonBody));

            //Execute the GetRequest
            CloseableHttpResponse response = httpClient.execute(createPackRequest);

            if(response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED){

                return response;
            }
            else {
                logger.error(String.format("Error executing the request ! Status Code: %d -> Response Body: %s",
                        response != null ? response.getStatusLine().getStatusCode() : -1,
                        response != null ? EntityUtils.toString(response.getEntity()) : null));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



}

