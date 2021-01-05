package smartBuilding.server.resource.coap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartBuilding.server.resource.raw.AlarmActuator;
import smartBuilding.server.resource.raw.PMRaw;
import smartBuilding.server.resource.raw.ResourceDataListener;
import smartBuilding.server.resource.raw.SmartObjectResource;
import utils.CoreInterfaces;
import utils.SenMLPack;
import utils.SenMLRecord;

import java.util.Optional;

public class CoapPMResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapPMResource.class);

    private final static String OBJECT_TITLE = "Presence Monitoring";

    private Double ACTUATOR_VERSION = 0.5;

    private PMRaw pmRaw;

    private String deviceId;
    private ObjectMapper objectMapper;


    public CoapPMResource(String name, String deviceId, PMRaw pmRaw) throws InterruptedException {
        super(String.format("%s:%s", deviceId,name));

        if (pmRaw != null && deviceId != null)
        {
            this.deviceId = deviceId;
            this.pmRaw = pmRaw;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", pmRaw.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));



        }
        else {
            logger.error(" ERROR -->NULL Raw References");
        }

    }


    private Optional<String> getJsonSenmlResponse() {

        try {

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, "presencemonitoring"));
            senMLRecord.setBver(ACTUATOR_VERSION);
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
    }
}
