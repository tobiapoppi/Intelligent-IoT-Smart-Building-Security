package SmartBuildingResources.Server.Resource.coap;

import SmartBuildingResources.Server.Resource.SmartBuildingCoapSmartObjectProcess;
import SmartBuildingResources.Server.Resource.raw.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CoreInterfaces;
import utils.SenMLPack;
import utils.SenMLRecord;

import java.util.Optional;
import java.util.UUID;

public class CoapAreaResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapAreaResource.class);

    private final static String OBJECT_TITLE = "Area";

    private static final String TARGET_LISTENING_IP = "192.168.1.107";

    private static final int TARGET_PORT = 5683;

    private AreaResource areaResource;

    private ObjectMapper objectMapper;


    public CoapAreaResource(String name, AreaResource areaResource) throws InterruptedException {
        super(name);


        if (areaResource != null)
        {
            this.areaResource = areaResource;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            this.add(createDeviceLight());
            this.add(createDeviceAlarm());
            this.add(createDevicePM());



            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", areaResource.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_B.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_LINK_FORMAT));


        }
        else {
            logger.error(" ERROR -->NULL Raw References");
        }

    }


    private CoapResource createDevicePir() throws InterruptedException {

        String deviceId = String.format("%s", UUID.randomUUID().toString());
        PirRawSensor pirRawSensor = new PirRawSensor();
        CoapPirResource coapPirResource = new CoapPirResource("pir", deviceId, pirRawSensor);


        return coapPirResource;
    };
    private CoapResource createDeviceAlarm() throws InterruptedException {

        String deviceId = String.format("%s", UUID.randomUUID().toString());
        AlarmActuator alarmRawSensor = new AlarmActuator();
        CoapAlarmResource coapAlarmResource = new CoapAlarmResource("alarm", deviceId, alarmRawSensor);


        return coapAlarmResource;
    };
    private CoapResource createDeviceLight() throws InterruptedException {

        String deviceId = String.format("%s", UUID.randomUUID().toString());
        LightActuator lightRawSensor = new LightActuator();
        CoapLightResource coapLightResource = new CoapLightResource("light", deviceId, lightRawSensor);


        return coapLightResource;
    };

    private CoapResource createDeviceCamera() throws InterruptedException {


        String deviceId = String.format("%s", UUID.randomUUID().toString());
        CameraRawSensor cameraRawSensor = new CameraRawSensor();
        CoapCameraResource coapCameraResource = new CoapCameraResource("camera", deviceId, cameraRawSensor);


        return coapCameraResource;
    };
    private CoapResource createDevicePM() throws InterruptedException {

        CoapResource PresenceMonitoringResource = new CoapResource("presenceMonitoring");


        PresenceMonitoringResource.add(createDeviceCamera());
        PresenceMonitoringResource.add(createDevicePir());

        return PresenceMonitoringResource;
    };


    private Optional<String> getJsonSenmlResponse() {

        try {

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s", this.getName()));
            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        } catch (Exception e) {
            return Optional.empty();
        }
    }
    @Override
    public void handleGET(CoapExchange exchange){

        if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void handlePUT(CoapExchange exchange){
        try{

            //If the request body is available
            if(exchange.getRequestPayload() != null){

                String submittedValue = new String(exchange.getRequestPayload());

                //Update internal status
                this.setName(String.format("area%s", submittedValue));
                logger.info("Resource Status Updated: {}", submittedValue);

                exchange.respond(CoAP.ResponseCode.CHANGED);

                SmartBuildingCoapSmartObjectProcess.registerToCoapResourceDirectory(this.getParent().getParent().getParent(), "CoapEndpointSmartObject", TARGET_LISTENING_IP, TARGET_PORT);

            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling PUT -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }
    @Override
    public void handleDELETE(CoapExchange exchange){
        try{

            //If the request body is available
            if(exchange.getRequestPayload() == null){

                //Update internal status

                this.delete();

                logger.info("Resource Status Updated: Area {} deleted", this.getName());

                exchange.respond(CoAP.ResponseCode.DELETED);
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling DELETE -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }
}
