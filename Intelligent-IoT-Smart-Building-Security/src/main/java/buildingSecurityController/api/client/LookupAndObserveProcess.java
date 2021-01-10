package buildingSecurityController.api.client;

import buildingSecurityController.api.model.GenericDeviceDescriptor;
import buildingSecurityController.api.model.PolicyDescriptor;
import buildingSecurityController.api.model.ResourceDescriptor;
import buildingSecurityController.api.services.OperatorAppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP.Code;
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


/*questo process ha lo scopo di ricevere l'albero delle risorse presenti nel resource directory, e di iniziare
automaticamente ad osservarle, implementando tutti i metody onLoad invocati in maniera asincrona.
l'observing va fatto su tutti e soli i sensori (no attuatori).
Contiene anche la parte di intelligenza vera e propria che decide, accedendo alle policy,
quando e se fare scattare luci e allarmi*/

public class LookupAndObserveProcess implements Runnable{

    private final static Logger logger = LoggerFactory.getLogger(LookupAndObserveProcess.class);
    private static final String TARGET_RD_IP = "edgeiotgateway.servehttp.com";
    private static final int TARGET_RD_PORT = 5683;
    private static final String RD_LOOKUP_URI = "/rd-lookup/res";

    private static final String OBSERVABLE_CORE_ATTRIBUTE = "obs";
    private static final String INTERFACE_CORE_ATTRIBUTE = "if";
    private static final String WELL_KNOWN_CORE_URI = "/.well-known/core";

    private static final String SMARTOBJECT_ENDPOINT = "coap://192.168.1.107:5683/";
    //private static final String SMARTOBJECT_ENDPOINT = "coap://192.168.1.59:5683/";

    private static List<String> pirTargetObservableList = null;
    private static List<String> camTargetObservableList = null;
    private static List<String> lightTargetObservableList = null;
    private static List<String> alarmTargetObservableList = null;
    private static List<String> presenceMonitoringTargetList = null;
    private static Map<String, CoapObserveRelation> observingRelationMap = null;

    @SuppressWarnings("serial")
    public static class MissingKeyException extends Exception{}
    final OperatorAppConfig conf;

    private static ObjectMapper objectMapper = new ObjectMapper();

    public LookupAndObserveProcess(OperatorAppConfig operatorAppConfig){
        this.conf = operatorAppConfig;
    }



    private void lookupTarget(CoapClient coapClient){

        //we set the type of request as a "Get"
        Request request = new Request(Code.GET);
        request.setURI(String.format("coap://%s:%d%s", TARGET_RD_IP, TARGET_RD_PORT, RD_LOOKUP_URI));
        request.setConfirmable(true);


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

                                } else if (webLink.getAttributes().getAttributeValues(RESOURCE_TYPE).contains("iot.sensor.presencemonitoring")){

                                    String targetResourceUrl = String.format("%s", webLink.getURI());
                                    if (!presenceMonitoringTargetList.contains(targetResourceUrl)){
                                        logger.info("Target Resource found! URI: {}", webLink.getURI());
                                        presenceMonitoringTargetList.add(targetResourceUrl);
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

        }catch (ConnectorException | IOException e){
            e.printStackTrace();
        }
    }


    private void handleNotificationPir(CoapResponse response, String targetUrl){
        try{
            //this is the method asynchronously invoked when an observed resource is sending data;

            String content = response.getResponseText();

            logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);

            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);

            Boolean pirValue = newPack.get(0).getVb();

            if(pirValue){
                //accedo alle policy

                List<PolicyDescriptor> policyList = this.conf.getInventoryDataManager().getPolicyList();

                List<GenericDeviceDescriptor> deviceList = this.conf.getInventoryDataManager().getDeviceList();

                //controllo se tutte le policy rispettano i cambiamenti

                //prima ottengo area e floor di questo pir

                List<String> areaId = new ArrayList<>();

                deviceList.forEach(device -> {
                    if(device.getDeviceId().equals(newPack.get(0).getBn())){
                        areaId.add(device.getAreaId());
                    }
                });

                //ora accedo alle policy che hanno lo stessa area e floor del pir in questione

                policyList.forEach(policy ->{
                    if (policy.getArea_id().equals(areaId.get(0))){
                        if(policy.getPresence_mode()){

                            int hourStart = Integer.parseInt(policy.getStart_working_time().split(":")[0]);
                            int minuteStart = Integer.parseInt(policy.getStart_working_time().split(":")[1]);
                            int hourFin = Integer.parseInt(policy.getEnd_working_time().split(":")[0]);
                            int minuteFin = Integer.parseInt(policy.getEnd_working_time().split(":")[1]);

                            int hour = java.time.LocalTime.now().getHour();
                            int minutes = java.time.LocalTime.now().getMinute();

                            if(hour > hourStart || hour < hourFin || hour == hourStart && minutes > minuteStart || hour == hourFin && minutes < minuteFin){

                                //I MAKE A PUT REQUEST
                                //se l'ora non è rispettata faccio una put request TRUE a tutti i device di luce e allarme nella stessa area

                                CoapResourceClient coapResourceClient = new CoapResourceClient();

                                List<String> targetUrlsAlarm = new ArrayList<>();

                                deviceList.forEach(device -> {
                                    logger.info("AREA {} DEVICE", areaId.get(0));
                                    if(device.getAreaId().equals(areaId.get(0))){
                                        if(device.getDeviceId().contains("alarm") || device.getDeviceId().contains("light")){
                                            targetUrlsAlarm.add(String.format("coap://192.168.1.107:5683/%s", device.getDeviceId()));
                                        }
                                    }

                                });
                                targetUrlsAlarm.forEach(url ->{
                                    CoapResponse coapResponse = coapResourceClient.putRequest(url, "true");
                                    logger.info("Response pretty print:\n{}", Utils.prettyPrint(coapResponse));
                                    logger.info("ALARMS AND LIGHTS ACTIVATED FOR AREA {}", areaId.get(0));
                                });

                            }
                        }
                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleNotificationCam(CoapResponse response, String targetUrl){
        try{
            //this is the method asynchronously invoked when an observed resource is sending data;

            String content = response.getResponseText();

            logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);

            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);

            Number camValue = newPack.get(0).getV();


            //accedo alle policy

            List<PolicyDescriptor> policyList = this.conf.getInventoryDataManager().getPolicyList();

            List<GenericDeviceDescriptor> deviceList = this.conf.getInventoryDataManager().getDeviceList();

            //controllo se tutte le policy rispettano i cambiamenti

            //prima ottengo area e floor di questo pir

            List<String> areaId = new ArrayList<>();

            deviceList.forEach(device -> {
                if(device.getDeviceId().equals(newPack.get(0).getBn())){
                    areaId.add(device.getAreaId());
                }
            });

            //ora accedo alle policy che hanno lo stessa area e floor del pir in questione

            policyList.forEach(policy ->{
                if (policy.getArea_id().equals(areaId.get(0))){
                    if(!policy.getPresence_mode() && policy.getMax_persons() < (Integer) camValue){

                        int hourStart = Integer.parseInt(policy.getStart_working_time().split(":")[0]);
                        int minuteStart = Integer.parseInt(policy.getStart_working_time().split(":")[1]);
                        int hourFin = Integer.parseInt(policy.getEnd_working_time().split(":")[0]);
                        int minuteFin = Integer.parseInt(policy.getEnd_working_time().split(":")[1]);

                        int hour = java.time.LocalTime.now().getHour();
                        int minutes = java.time.LocalTime.now().getMinute();

                        if(hour > hourStart || hour < hourFin || hour == hourStart && minutes > minuteStart || hour == hourFin && minutes < minuteFin){

                            //I MAKE A PUT REQUEST
                            //se l'ora non è rispettata faccio una put request TRUE a tutti i device di luce e allarme nella stessa area

                            CoapResourceClient coapResourceClient = new CoapResourceClient();

                            List<String> targetUrls = new ArrayList<>();

                            deviceList.forEach(device -> {
                                logger.info("AREA {} DEVICE", areaId.get(0));
                                if(device.getAreaId().equals(areaId.get(0))){
                                    if(device.getDeviceId().contains("alarm") || device.getDeviceId().contains("light")){
                                        targetUrls.add(String.format("coap://192.168.1.107:5683/%s", device.getDeviceId()));
                                    }
                                }
                            });
                            targetUrls.forEach(url ->{
                                CoapResponse coapResponse = coapResourceClient.putRequest(url, "true");
                                logger.info("Response pretty print:\n{}", Utils.prettyPrint(coapResponse));
                                logger.info("ALARMS AND LIGHTS ACTIVATED FOR AREA {}",  areaId.get(0));
                            });
                        }
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startObservingPir (CoapClient coapClient, String targetUrl) {

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleNotificationPir(response, targetUrl);
                    }
                }).start();

            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });

        CoapResourceClient coapResourceClient = new CoapResourceClient();
        CoapResponse response = coapResourceClient.getRequest(targetUrl);

        try{
            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);

            ResourceDescriptor newResource = new ResourceDescriptor();
            newResource.setDeviceId(newPack.get(0).getBn());
            newResource.setResourceId(String.format("%s:pir", newPack.get(0).getBn()));
            newResource.setType("iot.sensor.pir");
            newResource.setManufacturer("theBuildingSecurity.servehttp.com");
            newResource.setCoreInterface("core.s");

            this.conf.getInventoryDataManager().getDevice(newPack.get(0).getBn()).get().addValueToResourceList("pir");


            this.conf.getInventoryDataManager().createNewResource(newResource);

        }catch (Exception e){
            e.printStackTrace();
        }
        observingRelationMap.put(targetUrl, relation);

    }

    private void startObservingCam (CoapClient coapClient, String targetUrl){

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

        CoapResourceClient coapResourceClient = new CoapResourceClient();
        CoapResponse response = coapResourceClient.getRequest(targetUrl);

        try{
            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);

            ResourceDescriptor newResource = new ResourceDescriptor();
            newResource.setDeviceId(newPack.get(0).getBn());
            newResource.setResourceId(String.format("%s:camera", newPack.get(0).getBn()));
            newResource.setType("iot.sensor.camera");
            newResource.setManufacturer("theBuildingSecurity.servehttp.com");
            newResource.setCoreInterface("core.s");
            this.conf.getInventoryDataManager().getDevice(newPack.get(0).getBn()).get().addValueToResourceList("camera");

            conf.getInventoryDataManager().createNewResource(newResource);

        }catch (Exception e){
            e.printStackTrace();
        }
        observingRelationMap.put(targetUrl, relation);

    }

    private void startObservingLight (CoapClient coapClient, String targetUrl){

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

        CoapResourceClient coapResourceClient = new CoapResourceClient();
        CoapResponse response = coapResourceClient.getRequest(targetUrl);

        try{
            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);

            GenericDeviceDescriptor newDevice = new GenericDeviceDescriptor();
            newDevice.setDeviceId(newPack.get(0).getBn());
            newDevice.addValueToResourceList("light");
            ResourceDescriptor newResource = new ResourceDescriptor();
            newResource.setDeviceId(newPack.get(0).getBn());
            newResource.setResourceId(String.format("%s:light", newPack.get(0).getBn()));
            newResource.setType("iot.actuator.light");
            newResource.setManufacturer("theBuildingSecurity.servehttp.com");
            newResource.setCoreInterface("core.a");

            conf.getInventoryDataManager().createNewResource(newResource);
            conf.getInventoryDataManager().createNewDevice(newDevice);

            logger.info("New Device added to Inventory!: {}", newDevice);

        }catch (Exception e){
            e.printStackTrace();
        }

        observingRelationMap.put(targetUrl, relation);
    }

    private void startObservingAlarm (CoapClient coapClient, String targetUrl){

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

        CoapResourceClient coapResourceClient = new CoapResourceClient();
        CoapResponse response = coapResourceClient.getRequest(targetUrl);

        try{
            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);

            GenericDeviceDescriptor newDevice = new GenericDeviceDescriptor();
            newDevice.setDeviceId(newPack.get(0).getBn());
            newDevice.addValueToResourceList("alarm");
            ResourceDescriptor newResource = new ResourceDescriptor();
            newResource.setDeviceId(newPack.get(0).getBn());
            newResource.setResourceId(String.format("%s:alarm", newPack.get(0).getBn()));
            newResource.setType("iot.actuator.alarm");
            newResource.setManufacturer("theBuildingSecurity.servehttp.com");
            newResource.setCoreInterface("core.a");


            conf.getInventoryDataManager().createNewResource(newResource);
            conf.getInventoryDataManager().createNewDevice(newDevice);

            logger.info("New Device added to Inventory!: {}", newDevice);

        }catch (Exception e){
            e.printStackTrace();
        }

        observingRelationMap.put(targetUrl, relation);
    }

    private void addPresenceMonitoringToDevices (CoapClient coapClient, String targetUrl){

        logger.info("COLLECTING ... {}", targetUrl);

        CoapResourceClient coapResourceClient = new CoapResourceClient();
        CoapResponse response = coapResourceClient.getRequest(targetUrl);

        try{
            SenMLPack newPack = new ObjectMapper().readValue(response.getPayload(), SenMLPack.class);

            GenericDeviceDescriptor newDevice = new GenericDeviceDescriptor();
            newDevice.setDeviceId(newPack.get(0).getBn());

            conf.getInventoryDataManager().createNewDevice(newDevice);

            logger.info("New Device added to Inventory!: {}", newDevice);

        }catch (Exception e){
            e.printStackTrace();
        }
        observingRelationMap.put(targetUrl, null);

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
        presenceMonitoringTargetList = new ArrayList<>();

        while(true){

            lookupTarget(coapClient);

            //start observing resources
            presenceMonitoringTargetList.forEach(targetResourceUrl -> {
                if (!observingRelationMap.containsKey(targetResourceUrl))
                    addPresenceMonitoringToDevices(coapClient, targetResourceUrl);
            });

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
}
