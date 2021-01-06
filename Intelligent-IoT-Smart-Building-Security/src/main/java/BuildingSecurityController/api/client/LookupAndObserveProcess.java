package BuildingSecurityController.api.client;

import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.persistance.DefaultInventoryDataManager;
import smartBuilding.server.resource.coap.CoapCameraResource;
import smartBuilding.server.resource.coap.CoapPirResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.eclipse.californium.core.coap.LinkFormat.RESOURCE_TYPE;


/*questo process ha lo scopo di ricevere l'albero delle risorse presenti nel resource directory, e di iniziare
automaticamente ad osservarle, implementando tutti i metody onLoad invocati in maniera asincrona.
l'observing va fatto su tutti e soli i sensori (no attuatori). */

public class LookupAndObserveProcess implements Runnable{

    private final static Logger logger = LoggerFactory.getLogger(LookupAndObserveProcess.class);

    //private static final String COAP_ENDPOINT = "coap://127.0.0.1:<porta>/rd-lookup/res";

    private static final String TARGET_RD_IP = "thebuildingsecurity.servehttp.com";
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


//    public static List<String> getDevices(String uri){
//
//        List<String> formattedDevList = new ArrayList<>();
//        pirTargetObservableList.forEach(dev ->  {
//                    if(dev.contains(uri)){
//                        dev = dev.replace(String.format("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/%s/", uri), "");
//                        formattedDevList.add(dev);
//                    }
//                }
//        );
//        alarmTargetObservableList.forEach(dev ->  {
//                    if(dev.contains(uri)){
//                        dev = dev.replace(String.format("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/%s/", uri), "");
//                        formattedDevList.add(dev);
//                    }
//                }
//        );
//        camTargetObservableList.forEach(dev ->  {
//                    if(dev.contains(uri)){
//                        dev = dev.replace(String.format("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/%s/", uri), "");
//                        formattedDevList.add(dev);
//                    }
//                }
//        );
//        lightTargetObservableList.forEach(dev ->  {
//                    if(dev.contains(uri)){
//                        dev = dev.replace(String.format("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/%s/", uri), "");
//                        formattedDevList.add(dev);
//                    }
//                }
//        );
//        return formattedDevList;
//    }


    private static void lookupTarget(CoapClient coapClient){

        //we set the type of request as a "Get"
        Request request = new Request(Code.GET);
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
                            if (webLink.getAttributes().containsAttribute(OBSERVABLE_CORE_ATTRIBUTE) &&
                                    webLink.getAttributes().containsAttribute(INTERFACE_CORE_ATTRIBUTE)){
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
            //logger.info("Payload: {}", text);
            //logger.info("Message ID: " + coapResponse.advanced().getMID());
            //logger.info("Token: " + coapResponse.advanced().getTokenString());


            ///  HERE WE NEED A CODE TO PARSE ALL THE RES RECEIVED, LOOK WHICH WE NEED TO OBS AND ADD THEM TO HE OBSERVINGLIST ///

        }catch (ConnectorException | IOException e){
            e.printStackTrace();
        }
    }


    private static void handleNotification(CoapResponse response, String targetUrl){
        try{


            //this is the method asynchronously invoked when an observed resource is sending data;

            String content = response.getResponseText();

            logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);

            if(content.equals("true")){
                //accedo alle policy

                HashMap<String, PolicyDescriptor> policyMap = DefaultInventoryDataManager.getPolicies();

                //controllo se tutte le policy rispettano i cambiamenti

                //prima ottengo area e floor di questo pir

                String floor = targetUrl.replace("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/", "").split("/")[0];
                String area = targetUrl.replace("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/", "").split("/")[1];


                logger.info("Sto accedendo alle risorse.");

                policyMap.values().forEach(policy ->{
                    if (policy.getFloor_id().equals(floor) && policy.getArea_id().equals(area)){
                        if(policy.getPresence_mode()){

                            int hourStart = Integer.parseInt(policy.getStart_working_time().split(":")[0]);
                            int minuteStart = Integer.parseInt(policy.getStart_working_time().split(":")[1]);
                            int hourFin = Integer.parseInt(policy.getEnd_working_time().split(":")[0]);
                            int minuteFin = Integer.parseInt(policy.getEnd_working_time().split(":")[1]);

                            int hour = java.time.LocalTime.now().getHour();
                            int minutes = java.time.LocalTime.now().getMinute();

                            logger.info("Ora faccio i controlli sull'ora" );

                            logger.info("{} {}", hour, minutes);



                            if(hour > hourStart || hour < hourFin){

                                //I MAKE A PUT REQUEST

                                CoapResourceClient coapResourceClient = new CoapResourceClient();

                                CoapResponse coapResponse = coapResourceClient.putRequest(String.format("%s/%s/alarm", floor, area), "true");


                                logger.info("ALARM ACTIVATED FOR FLOOR {} AREA {}", floor, area);

                            }else if(hour == hourStart && minutes > minuteStart){
                                //CoapResponse coapResponse = coapResourceClient.putRequest(String.format("%s/%s/alarm", floor, area), "true");
                                logger.info("ALARM ACTIVATED FOR FLOOR {} AREA {}", floor, area);


                            }else if(hour == hourFin && minutes < minuteFin){
                                //CoapResponse coapResponse = coapResourceClient.putRequest(String.format("%s/%s/alarm", floor, area), "true");
                                logger.info("ALARM ACTIVATED FOR FLOOR {} AREA {}", floor, area);

                            }
                        }
                    }
                });

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void startObservingPir (CoapClient coapClient, String targetUrl) {

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleNotification(response, targetUrl);
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

                //this is the method asynchronously invoked when an observed resource is sending data;

                String content = response.getResponseText();

                logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);


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

                //this is the method asynchronously invoked when an observed resource is sending data;

                String content = response.getResponseText();

                logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);

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

                //this is the method asynchronously invoked when an observed resource is sending data;

                String content = response.getResponseText();

                logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);

            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });
        observingRelationMap.put(targetUrl, relation);
    }



    @Override
    public void run() {
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
            pirTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingPir(coapClient, targetResourceUrl);
            });
            camTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingCam(coapClient, targetResourceUrl);
            });


            lightTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingLight(coapClient, targetResourceUrl);
            });
            alarmTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingAlarm(coapClient, targetResourceUrl);
            });

            try {
                Thread.sleep(1000*60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
