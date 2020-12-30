package SmartBuildingResources.Server.Resource;

import SmartBuildingResources.Server.Resource.coap.*;
import SmartBuildingResources.Server.Resource.raw.*;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class SmartBuildingCoapSmartObjectProcess extends CoapServer {

    private static final String RD_COAP_ENDPOINT_BASE_URL = "coap://192.168.1.108:5683/rd";

    private static final String TARGET_LISTENING_IP = "192.168.1.107";

    private static final int TARGET_PORT = 5683;

    private final static Logger logger = LoggerFactory.getLogger(SmartBuildingCoapSmartObjectProcess.class);

    public SmartBuildingCoapSmartObjectProcess() throws InterruptedException {

        super();
        String deviceId = String.format("%s", UUID.randomUUID().toString());

        this.add(createFloorResource(deviceId, 0));

    }

    private CoapResource createFloorResource(String deviceId, Integer floorNumber) throws InterruptedException {

        FloorResource floorResource = new FloorResource();
        CoapFloorResource coapFloorResource = new CoapFloorResource(String.format("floor%s", floorNumber), floorResource);

        //CoapResource floorResource = new CoapResource(String.format("floor%s", floorNumber));
        coapFloorResource.add(createAreaResource(deviceId, "A"));
        return coapFloorResource;
    }

    private CoapResource createAreaResource(String deviceId, String areaId) throws InterruptedException {

        CoapResource areaResource = new CoapResource(String.format("area%s", areaId));
        LightActuator pmlightactuator = new LightActuator();
        CoapLightResource PMLightResource = new CoapLightResource("light", deviceId, pmlightactuator);

        AlarmActuator pmalarmactuator = new AlarmActuator();
        CoapAlarmResource PMalarmResource = new CoapAlarmResource("alarm", deviceId, pmalarmactuator);


        areaResource.add(CreatePresenceMonitoringResource(deviceId));
        areaResource.add(PMLightResource);
        areaResource.add(PMalarmResource);

        return areaResource;

    };

    private CoapResource CreatePresenceMonitoringResource(String deviceId) {
        CoapResource PresenceMonitoringResource = new CoapResource("presenceMonitoring");
        PirRawSensor PMPirRawSensor = new PirRawSensor();
        CameraRawSensor PMCameraRawSensor = new CameraRawSensor();
        CoapPirResource PMcoapPirResource = new CoapPirResource("pir", deviceId, PMPirRawSensor);
        CoapCameraResource PMcoapCameraRecource = new CoapCameraResource("camera", deviceId,PMCameraRawSensor );

        PresenceMonitoringResource.add(PMcoapPirResource);
        PresenceMonitoringResource.add(PMcoapCameraRecource);

        return PresenceMonitoringResource;
    };

    //TODO metodo "registerToCoapResourceDirectory" - VIDEO LABORATORIO 8 PARTE 2 MINUTO 12:00
    //serve per registrare tutte le risorse dell'object sul resource directory

    private static void registerToCoapResourceDirectory(Resource rootResource, String endpointName, String sourceIp, int sourcePort){

        String finalRdUrl = String.format("%s?ep=%s&base=coap://%s:%d", RD_COAP_ENDPOINT_BASE_URL, endpointName, sourceIp, sourcePort);

        logger.info("Registering to Resource Directory: {}", finalRdUrl);

        CoapClient coapClient = new CoapClient(finalRdUrl);
        Request request = new Request(CoAP.Code.POST);

        request.setPayload(LinkFormat.serializeTree(rootResource));
        request.setConfirmable(true);

        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

        //bloccante, aspetta la risposta
        CoapResponse coapResponse = null;

        try{
            coapResponse = coapClient.advanced(request);

            logger.info("Response Pretty Print:\n{}", Utils.prettyPrint(coapResponse));
            String text = coapResponse.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message Id: "+ coapResponse.advanced().getMID());
            logger.info("Token: "+ coapResponse.advanced().getTokenString());

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }

    }



    public static void main(String[] args) throws InterruptedException {

        SmartBuildingCoapSmartObjectProcess smartBuildingCoapSmartObjectProcess = new SmartBuildingCoapSmartObjectProcess();
        smartBuildingCoapSmartObjectProcess.start();

        Thread.sleep(2000);

        logger.info("Coap Server Started ! Available resources: ");

        smartBuildingCoapSmartObjectProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("-> URI: {} \t(Observable: {})", resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("-> URI:\t {} \t(Observable: {})", childResource.getURI(), childResource.isObservable());
                    childResource.getChildren().stream().forEach(subChild -> {
                        logger.info("-> URI:\t\t {} \t(Observable: {})", subChild.getURI(), subChild.isObservable());
                        subChild.getChildren().stream().forEach(terminalResource -> {
                            logger.info("-> URI:\t\t\t {} \t(Observable: {})", terminalResource.getURI(), terminalResource.isObservable());
                        });
                    });
                });
            }
        });

        registerToCoapResourceDirectory(smartBuildingCoapSmartObjectProcess.getRoot(),
                "CoapEndpointSmartObject", TARGET_LISTENING_IP, TARGET_PORT);



    }


}

