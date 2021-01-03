package BuildingSecurityController.api.client;

import SmartBuildingResources.Server.Resource.coap.CoapCameraResource;
import SmartBuildingResources.Server.Resource.coap.CoapPirResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CoreInterfaces;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.eclipse.californium.core.coap.LinkFormat.RESOURCE_TYPE;


/*questo process ha lo scopo di ricevere l'albero delle risorse presenti nel resource directory, e di iniziare
automaticamente ad osservarle, implementando tutti i metody onLoad invocati in maniera asincrona.
l'observing va fatto su tutti e soli i sensori (no attuatori). */

public class LookupAndObserveProcess implements Runnable{

    private final static Logger logger = LoggerFactory.getLogger(LookupAndObserveProcess.class);

    //private static final String COAP_ENDPOINT = "coap://127.0.0.1:<porta>/rd-lookup/res";

    private static final String TARGET_RD_IP = "192.168.1.108";
    private static final int TARGET_RD_PORT = 5683;
    private static final String RD_LOOKUP_URI = "/rd-lookup/res";

    private static final String OBSERVABLE_CORE_ATTRIBUTE = "obs";
    private static final String INTERFACE_CORE_ATTRIBUTE = "if";
    private static final String WELL_KNOWN_CORE_URI = "/.well-known/core";

    private static List<String> pirTargetObservableList = null;
    private static List<String> camTargetObservableList = null;
    private static List<String> lightTargetObservableList = null;
    private static List<String> alarmTargetObservableList = null;
    private static List<String> floorTargetObservableList = null;
    private static List<String> areaTargetObservableList = null;
    private static Map<String, CoapObserveRelation> observingRelationMap = null;
    private static CoapPirResource newPir = null;
    private static CoapCameraResource newCam = null;

    private static ObjectMapper objectMapper = new ObjectMapper();


    public static void main(String[] args) throws InterruptedException {

        //init coap client
        CoapClient coapClient = new CoapClient();

        //init target resource list array and observing relations
        pirTargetObservableList = new ArrayList<>();
        camTargetObservableList = new ArrayList<>();
        lightTargetObservableList = new ArrayList<>();
        alarmTargetObservableList = new ArrayList<>();
        floorTargetObservableList = new ArrayList<>();
        areaTargetObservableList = new ArrayList<>();
        observingRelationMap = new HashMap<>();

        //discover all available sensors and actuators

        while(true){
            lookupTarget(coapClient);

            //start observing resources
            pirTargetObservableList.forEach(targetResourceUrl -> {
                startObservingPir(coapClient, targetResourceUrl);
            });
            camTargetObservableList.forEach(targetResourceUrl -> {
                startObservingCam(coapClient, targetResourceUrl);
            });
            floorTargetObservableList.forEach(targetResourceUrl -> {
                try {
                    startObservingFloor(coapClient, targetResourceUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            areaTargetObservableList.forEach(targetResourceUrl -> {
                startObservingArea(coapClient, targetResourceUrl);
            });
            lightTargetObservableList.forEach(targetResourceUrl -> {
                startObservingLight(coapClient, targetResourceUrl);
            });
            alarmTargetObservableList.forEach(targetResourceUrl -> {
                startObservingAlarm(coapClient, targetResourceUrl);
            });
            Thread.sleep(5000);
        }

    }

    public static List<String> getFloors(){

        List<String> formattedFloorsList = new ArrayList<>();
        floorTargetObservableList.forEach(floor ->  {
                    floor = floor.replace("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/", "");
                    formattedFloorsList.add(floor);
                }
        );
        return formattedFloorsList;
    }
    public static List<String> getAreas(String floorId){

        List<String> formattedAreaList = new ArrayList<>();
        areaTargetObservableList.forEach(area ->  {
                if(area.contains(floorId)){
                    area = area.replace(String.format("coap://192.168.1.107:5683/buildingPoppiZaniboniInc/%s/", floorId), "");
                    formattedAreaList.add(area);
                    logger.info("la lista delle aree trovate è : {}", formattedAreaList);
                }
            }
        );
        return formattedAreaList;
    }
    public static List<String> getDevices(String uriPrecedente){

        List<String> formattedList = new ArrayList<>();
        pirTargetObservableList.forEach(device -> {
            device = device.replace("coap://192.168.1.107:5683/", "");
                    formattedList.add(device);

                });
        camTargetObservableList.forEach(device -> {
            device = device.replace("coap://192.168.1.107:5683/", "");
            formattedList.add(device);
        });
        lightTargetObservableList.forEach(device -> {
            device = device.replace("coap://192.168.1.107:5683/", "");
            formattedList.add(device);
        });
        alarmTargetObservableList.forEach(device -> {
            device = device.replace("coap://192.168.1.107:5683/", "");
            formattedList.add(device);
        });
        return formattedList;
    }


    private static void lookupTarget(CoapClient coapClient){

        //we set the type of request as a "Get"
        Request request = new Request(Code.GET);
        request.setURI(String.format("coap://%s:%d%s", TARGET_RD_IP, TARGET_RD_PORT, RD_LOOKUP_URI));
        request.setConfirmable(true);

        logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));



        //the system is synchronous, will wait the response (blocking)
        CoapResponse coapResponse = null;

        try{
            coapResponse = coapClient.advanced(request);


            //la risposta non deve essere nulla
            if(coapResponse != null){

                logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResponse));

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

                                    logger.info("Target Resource found! URI: {}", webLink.getURI());

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!pirTargetObservableList.contains(targetResourceUrl))
                                        pirTargetObservableList.add(targetResourceUrl);

                                    logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);

                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.sensor.camera")){

                                    logger.info("Target Resource found! URI: {}", webLink.getURI());

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!camTargetObservableList.contains(targetResourceUrl))
                                        camTargetObservableList.add(targetResourceUrl);
                                    logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.actuator.light")){

                                    logger.info("Target Resource found! URI: {}", webLink.getURI());

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!lightTargetObservableList.contains(targetResourceUrl))
                                        lightTargetObservableList.add(targetResourceUrl);
                                    logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.actuator.alarm")){

                                    logger.info("Target Resource found! URI: {}", webLink.getURI());

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!alarmTargetObservableList.contains(targetResourceUrl))
                                        alarmTargetObservableList.add(targetResourceUrl);
                                    logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.floor")){

                                    logger.info("Target Resource found! URI: {}", webLink.getURI());

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!floorTargetObservableList.contains(targetResourceUrl))
                                        floorTargetObservableList.add(targetResourceUrl);
                                    logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.area")){

                                    logger.info("Target Resource found! URI: {}", webLink.getURI());

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!areaTargetObservableList.contains(targetResourceUrl))
                                        areaTargetObservableList.add(targetResourceUrl);
                                    logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);
                                }
                            }
                            else{
                                logger.info("Resource {} does not match filtering parameters.", webLink.getURI());
                            }
                        }
                    });
                }
                else{
                    logger.info("Core Link Format Response not found.");
                }
            }

            String text = coapResponse.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResponse.advanced().getMID());
            logger.info("Token: " + coapResponse.advanced().getTokenString());



            ///  HERE WE NEED A CODE TO PARSE ALL THE RES RECEIVED, LOOK WHICH WE NEED TO OBS AND ADD THEM TO HE OBSERVINGLIST ///




        }catch (ConnectorException | IOException e){
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

                //this is the method asynchronously invoked when an observed resource is sending data;

                String content = response.getResponseText();

                //TODO PER NIENTE SICURO, C'è DA PROVARE SE VA

                logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);

                /*TODO QUI DEVO ANALIZZARE "content" E CONTROLLARE CHE TUTTI I CAMBIAMENTI CHE SONO AVVENUTI NEL SENSORE,
                SIANO CONSONI CON TUTTE LE POLICY DEL BUILDING, E IN CASO CONTRARIO, FAR SCATTARE L'ALLARME COLLEATO
                ALLA ZONA NELLA QUALE E' STATA INFRANTA UNA REGOLA.*/


                /*IL CONTROLLO VERRA' INVOCATO DA QUI E SARA' SVOLTO DAL "INVENTORY DATA MANAGER" IN QUANTO E'
                L'UNICO COMPONTE AD AVERE CONTROLLO ED ACCESSO ALLE RISORSE ARCHIVIATE IN MEMORIA RAM.*/

                //TODO
                /*DECIDERE SE E' MEGLIO LASCIARE TUTTO SU RAM O SE SCRIVERE SU FILE PER POI ACCEDERE ALL'ARCHIVIO
                DA ALTRI OGGETTI */
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
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {

                //this is the method asynchronously invoked when an observed resource is sending data;

                String content = response.getResponseText();

                //TODO PER NIENTE SICURO, C'è DA PROVARE SE VA

                logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);

                /*TODO QUI DEVO ANALIZZARE "content" E CONTROLLARE CHE TUTTI I CAMBIAMENTI CHE SONO AVVENUTI NEL SENSORE,
                SIANO CONSONI CON TUTTE LE POLICY DEL BUILDING, E IN CASO CONTRARIO, FAR SCATTARE L'ALLARME COLLEATO
                ALLA ZONA NELLA QUALE E' STATA INFRANTA UNA REGOLA.*/


                /*IL CONTROLLO VERRA' INVOCATO DA QUI E SARA' SVOLTO DAL "INVENTORY DATA MANAGER" IN QUANTO E'
                L'UNICO COMPONTE AD AVERE CONTROLLO ED ACCESSO ALLE RISORSE ARCHIVIATE IN MEMORIA RAM.*/

                //TODO
                /*DECIDERE SE E' MEGLIO LASCIARE TUTTO SU RAM O SE SCRIVERE SU FILE PER POI ACCEDERE ALL'ARCHIVIO
                DA ALTRI OGGETTI */
            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });


        observingRelationMap.put(targetUrl, relation);


    }

    private static void startObservingFloor (CoapClient coapClient, String targetUrl) throws IOException {

        FileWriter writer = new FileWriter("floors.txt");
        for(String str: floorTargetObservableList) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
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
    private static void startObservingArea (CoapClient coapClient, String targetUrl){

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
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
        pirTargetObservableList = new ArrayList<>();
        camTargetObservableList = new ArrayList<>();
        lightTargetObservableList = new ArrayList<>();
        alarmTargetObservableList = new ArrayList<>();
        floorTargetObservableList = new ArrayList<>();
        areaTargetObservableList = new ArrayList<>();
        observingRelationMap = new HashMap<>();

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
            floorTargetObservableList.forEach(targetResourceUrl -> {
                try {
                    if (!observingRelationMap.containsKey(targetResourceUrl))
                        startObservingFloor(coapClient, targetResourceUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            areaTargetObservableList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    startObservingArea(coapClient, targetResourceUrl);
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
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
