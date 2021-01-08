package smartBuilding.server.resource;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import smartBuilding.server.resource.coap.*;
import smartBuilding.server.resource.raw.*;
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

    private static final String RD_COAP_ENDPOINT_BASE_URL = "coap://edgeiotgateway.servehttp.com:5683/rd";

    private static final String TARGET_LISTENING_IP = "192.168.1.107";

    private static final int TARGET_PORT = 5683;

    private final static Logger logger = LoggerFactory.getLogger(SmartBuildingCoapSmartObjectProcess.class);

    public SmartBuildingCoapSmartObjectProcess() throws InterruptedException {

        super();

        this.add(createPresenceMonitoringResource());
        this.add(createAlarmResource());
        this.add(createLightResource());

  }


    private static CoapResource createPresenceMonitoringResource() throws InterruptedException {

        String deviceId = String.format("%s", UUID.randomUUID().toString());

        PMRaw pmRaw = new PMRaw();
        CoapPMResource coapPMResource = new CoapPMResource ("presencemonitoring","0001", pmRaw);
        PirRawSensor PMPirRawSensor = new PirRawSensor();
        CameraRawSensor PMCameraRawSensor = new CameraRawSensor();
        CoapPirResource PMcoapPirResource = new CoapPirResource ("pir","0001", PMPirRawSensor);
        CoapCameraResource PMcoapCameraRecource = new CoapCameraResource("camera", "0001",PMCameraRawSensor );

        coapPMResource.add(PMcoapPirResource);
        coapPMResource.add(PMcoapCameraRecource);

        return coapPMResource;

    };

    private static CoapResource createAlarmResource () throws InterruptedException {
        String deviceId = String.format("%s", UUID.randomUUID().toString());
        AlarmActuator alarmRawSensor = new AlarmActuator();
        CoapAlarmResource coapAlarmResource = new CoapAlarmResource ("alarm","0004", alarmRawSensor);
        return coapAlarmResource;

    };
    private static CoapResource createLightResource () throws InterruptedException {
        String deviceId = String.format("%s", UUID.randomUUID().toString());
        LightActuator lightRawSensor = new LightActuator();
        CoapLightResource coapLightResource = new CoapLightResource ("light","0005", lightRawSensor);
        return coapLightResource;

    };



    public void registerToCoapResourceDirectory(SmartBuildingCoapSmartObjectProcess smartBuildingCoapSmartObjectProcess, String endpointName, String sourceIp, int sourcePort){

        String finalRdUrl = String.format("%s?ep=%s&base=coap://%s:%d", RD_COAP_ENDPOINT_BASE_URL, endpointName, sourceIp, sourcePort);

        logger.info("Registering to Resource Directory: {}", finalRdUrl);

        logger.info("{}", (smartBuildingCoapSmartObjectProcess.getRoot()).toString());

        CoapClient coapClient = new CoapClient(finalRdUrl);
        Request request = new Request(CoAP.Code.POST);

        request.setPayload(LinkFormat.serializeTree(smartBuildingCoapSmartObjectProcess.getRoot()));
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

        while(true){

            logger.info("Root è: {}", smartBuildingCoapSmartObjectProcess.getRoot().toString());

            smartBuildingCoapSmartObjectProcess.registerToCoapResourceDirectory(smartBuildingCoapSmartObjectProcess,
                    "CoapEndpointSmartObject", TARGET_LISTENING_IP, TARGET_PORT);
            Thread.sleep(1000*60*2);
        }


    }


}

