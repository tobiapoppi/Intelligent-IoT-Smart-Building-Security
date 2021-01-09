package buildingSecurityController.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SenMLPack;

import java.io.IOException;
import java.util.*;

import static org.eclipse.californium.core.coap.LinkFormat.RESOURCE_TYPE;

public class DataFetcher {
    private final static Logger logger = LoggerFactory.getLogger(LookupAndObserveProcess.class);
    private static final String TARGET_RD_IP = "edgeiotgateway.servehttp.com";
    private static final int TARGET_RD_PORT = 5683;
    private static final String RD_LOOKUP_URI = "/rd-lookup/res";

    private static final String OBSERVABLE_CORE_ATTRIBUTE = "obs";
    private static final String INTERFACE_CORE_ATTRIBUTE = "if";
    private static final String WELL_KNOWN_CORE_URI = "/.well-known/core";

    private static final String SMARTOBJECT_ENDPOINT = "coap://192.168.1.107:5683/";

    private static List<String> pirTargetObservableList = null;
    private static List<String> camTargetObservableList = null;
    private static List<String> lightTargetObservableList = null;
    private static List<String> alarmTargetObservableList = null;
    private static Map<String, CoapObserveRelation> observingRelationMap = null;


    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args){

        //init coap client
        CoapClient coapClient = new CoapClient();

        //init target resource list array and observing relations
        observingRelationMap = new HashMap<>();
        pirTargetObservableList = new ArrayList<>();
        camTargetObservableList = new ArrayList<>();
        lightTargetObservableList = new ArrayList<>();
        alarmTargetObservableList = new ArrayList<>();

        while(true){

            lookupTarget(coapClient);

            //start observing resources

            lightTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingLight(coapClient, targetResourceUrl);
            });
            alarmTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingAlarm(coapClient, targetResourceUrl);
            });


            pirTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingPir(coapClient, targetResourceUrl);
            });
            camTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingCam(coapClient, targetResourceUrl);
            });


            try {
                Thread.sleep(1000*60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static void startObservingPir (CoapClient coapClient, String targetUrl) {

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                logger.info("PROVO A FARE LA POST DEL PIR!!!!!!");

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        CloudPostClient cloudPostClient = new CloudPostClient();
                        try {
                            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);
                            cloudPostClient.postRequestToCloud(newPack);

                            logger.info("pack delivered to Cloud");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });
        observingRelationMap.put(targetUrl, relation);

    }

    private static void startObservingCam (CoapClient coapClient, String targetUrl){

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
        //request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CloudPostClient cloudPostClient = new CloudPostClient();
                        try {
                            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);
                            CloseableHttpResponse httpresponse = cloudPostClient.postRequestToCloud(newPack);

                            logger.info("pack delivered to Cloud");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });
        observingRelationMap.put(targetUrl, relation);

    }

    private static void startObservingLight (CoapClient coapClient, String targetUrl){

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
        //request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CloudPostClient cloudPostClient = new CloudPostClient();
                        try {
                            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);
                            CloseableHttpResponse httpresponse = cloudPostClient.postRequestToCloud(newPack);

                            logger.info("pack delivered to Cloud");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });
        observingRelationMap.put(targetUrl, relation);
    }

    private static void startObservingAlarm (CoapClient coapClient, String targetUrl){

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
        //request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CloudPostClient cloudPostClient = new CloudPostClient();
                        try {
                            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);
                            CloseableHttpResponse httpresponse = cloudPostClient.postRequestToCloud(newPack);

                            logger.info("pack delivered to Cloud");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });
        observingRelationMap.put(targetUrl, relation);
    }

    private static void lookupTarget(CoapClient coapClient){

        //we set the type of request as a "Get"
        Request request = new Request(CoAP.Code.GET);
        request.setURI(String.format("coap://%s:%d%s", TARGET_RD_IP, TARGET_RD_PORT, RD_LOOKUP_URI));
        request.setConfirmable(true);

        //logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));

        //the system is synchronous, will wait the response (blocking)
        CoapResponse coapResponse = null;

        try{
            coapResponse = coapClient.advanced(request);


            //la risposta non deve essere nulla
            if(coapResponse != null){

                //la risposta non deve essere di un media type che non sia CORE LINK FORMAT
                if (coapResponse.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){

                    Set<WebLink> links = LinkFormat.parse(coapResponse.getResponseText());

                    links.forEach(webLink -> {

                        //evito di includere wellknowncore alla lista delle risorse che ricevo.
                        if(webLink.getURI() != null && !webLink.getURI().equals(WELL_KNOWN_CORE_URI) && webLink.getAttributes() != null && webLink.getAttributes().getCount() > 0){

                            //considero solamente le risorse osservabili, che abbiano interface core attribute, e che sia di tipo sensor
                            if (webLink.getAttributes().containsAttribute(INTERFACE_CORE_ATTRIBUTE)){
                                if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.sensor.pir")){

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!pirTargetObservableList.contains(targetResourceUrl)) {
                                        logger.info("Target Resource found! URI: {}", webLink.getURI());
                                        pirTargetObservableList.add(targetResourceUrl);
                                        logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                    }


                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.sensor.camera")){


                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!camTargetObservableList.contains(targetResourceUrl)) {
                                        logger.info("Target Resource found! URI: {}", webLink.getURI());
                                        camTargetObservableList.add(targetResourceUrl);
                                        logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                    }

                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.actuator.light")){


                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!lightTargetObservableList.contains(targetResourceUrl)) {
                                        logger.info("Target Resource found! URI: {}", webLink.getURI());
                                        lightTargetObservableList.add(targetResourceUrl);
                                        logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                    }

                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.actuator.alarm")){


                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!alarmTargetObservableList.contains(targetResourceUrl)){
                                        logger.info("Target Resource found! URI: {}", webLink.getURI());
                                        alarmTargetObservableList.add(targetResourceUrl);
                                        logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                    }

                                }
                            }
                        }
                    });
                }
                else{
                    logger.info("Core Link Format Response not found.");
                }
            }

            String text = coapResponse.getResponseText();
        }catch (ConnectorException | IOException e){
            e.printStackTrace();
        }
    }




}
