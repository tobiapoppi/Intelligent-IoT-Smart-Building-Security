package smartBuilding.server.resource.coap;

import smartBuilding.server.resource.raw.PirRawSensor;
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

public class  CoapPirResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapPirResource.class);

    private final static String OBJECT_TITLE = "PirSensor";

    private String UNIT = "SEEING";

    private Double SENSOR_VERSION = 0.5;

    private String deviceId;

    private Boolean value = false;

    private PirRawSensor pirRawSensor;

    private ObjectMapper objectMapper;


    public CoapPirResource(String name, String deviceId, PirRawSensor pirRawSensor) {
        super(String.format("%s:%s", deviceId,name));

        if (pirRawSensor != null && deviceId != null)
        {

            this.deviceId = deviceId;
            this.pirRawSensor = pirRawSensor;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", pirRawSensor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

            pirRawSensor.addDataListener(new ResourceDataListener<Boolean>() {
                @Override
                public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedSensorValue) {

                    value = updatedSensorValue;
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

            SenMLRecord senMLRecordValue = new SenMLRecord();
            senMLRecordValue.setBn(String.format("%s:%s", this.deviceId, "presencemonitoring"));
            senMLRecordValue.setBver(SENSOR_VERSION);
            senMLRecordValue.setU(UNIT);
            senMLRecordValue.setVb(value);
            senMLRecordValue.setT(System.currentTimeMillis());


            senMLPack.add(senMLRecordValue);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

        @Override
    public void handleGET (CoapExchange exchange)
    {

        exchange.setMaxAge(PirRawSensor.UPDATE_PERIOD);


        Optional<String> senmlPayload = getJsonSenmlResponse();

        if(senmlPayload.isPresent())
            exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
        else
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);


    }

}



