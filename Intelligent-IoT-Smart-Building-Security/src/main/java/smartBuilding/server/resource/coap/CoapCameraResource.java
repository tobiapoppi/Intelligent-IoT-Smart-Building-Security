package smartBuilding.server.resource.coap;

import smartBuilding.server.resource.raw.CameraRawSensor;
import smartBuilding.server.resource.raw.ResourceDataListener;
import smartBuilding.server.resource.raw.SmartObjectResource;
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

public class CoapCameraResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapCameraResource.class);

    private final static String OBJECT_TITLE =" CameraSensor";

    private String UNIT = "Num";

    private Double SENSOR_VERSION = 0.5;

    private String deviceId;

    private Integer value = 0;

    private CameraRawSensor cameraRawSensor;

    private ObjectMapper objectMapper;


    public CoapCameraResource(String name, String deviceId, CameraRawSensor cameraRawSensor) {
        super(String.format("%s:%s", deviceId,name));

        if (cameraRawSensor != null && deviceId != null)
        {
            this.deviceId = deviceId;
            this.cameraRawSensor = cameraRawSensor;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", cameraRawSensor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

        }
        else {
            logger.error(" ERROR -->NULL Raw References");
        }

        this.cameraRawSensor.addDataListener(new ResourceDataListener<Integer>() {
            @Override
            public void onDataChanged(SmartObjectResource<Integer> resource, Integer updatedSensorValue) {

                value=updatedSensorValue;
                changed();
            }
        });


    }
    private Optional<String> getJsonSenmlResponse() {

        try {
            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId,"camera"));
            senMLRecord.setBver(SENSOR_VERSION);
            senMLRecord.setU(UNIT);
            senMLRecord.setV(value);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

        @Override
    public void handleGET (CoapExchange exchange)
    {

        exchange.setMaxAge(CameraRawSensor.UPDATE_PERIOD);

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
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(value), MediaTypeRegistry.TEXT_PLAIN);


    }

}
