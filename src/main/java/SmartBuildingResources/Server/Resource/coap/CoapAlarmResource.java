package SmartBuildingResources.Server.Resource.coap;

import SmartBuildingResources.Server.Resource.raw.AlarmActuator;
import SmartBuildingResources.Server.Resource.raw.LightActuator;
import SmartBuildingResources.Server.Resource.raw.ResourceDataListener;
import SmartBuildingResources.Server.Resource.raw.SmartObjectResource;
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

public class CoapAlarmResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapAlarmResource.class);

    private final static String OBJECT_TITLE =" Light Actuator";

    private Double ACTUATOR_VERSION = 0.5;

    private AlarmActuator alarmActuator;

    private Boolean Is_Active=false;

    private String deviceId;
    private ObjectMapper objectMapper;


    public CoapAlarmResource(String name, String deviceId, AlarmActuator alarmActuator) {
        super(name);

        if (alarmActuator != null && deviceId != null)
        {
            this.deviceId = deviceId;
            this.alarmActuator = alarmActuator;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", alarmActuator.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

            alarmActuator.addDataListener(new ResourceDataListener<Boolean>() {
                @Override
                public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                    logger.info("Raw Resource Notification. New Value: {}", updatedValue);
                    Is_Active=updatedValue;
                    changed();
                }
            });

        }
        else {
            logger.error(" ERROR -->NULL Raw References");
        }

    }
    private Optional<String> getJsonSenmlResponse() {

        try {

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setBver(ACTUATOR_VERSION);
            senMLRecord.setVb(Is_Active);
            senMLRecord.setT(System.currentTimeMillis());

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
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(Is_Active), MediaTypeRegistry.TEXT_PLAIN);
    }
    @Override
    public void handlePOST(CoapExchange exchange){
        try{
            //Empty request
            if(exchange.getRequestPayload() == null){

                //Update internal status
                this.Is_Active = !Is_Active;
                this.alarmActuator.setActive(Is_Active);

                logger.info("Resource Status Updated: {}", this.Is_Active);

                exchange.respond(CoAP.ResponseCode.CHANGED);
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public void handlePUT(CoapExchange exchange){
        try{

            //If the request body is available
            if(exchange.getRequestPayload() != null){

                boolean submittedValue = Boolean.parseBoolean(new String(exchange.getRequestPayload()));

                logger.info("Submitted value: {}", submittedValue);

                //Update internal status
                this.Is_Active = submittedValue;
                this.alarmActuator.setActive(this.Is_Active);

                logger.info("Resource Status Updated: {}", this.Is_Active);

                exchange.respond(CoAP.ResponseCode.CHANGED);
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }
}
