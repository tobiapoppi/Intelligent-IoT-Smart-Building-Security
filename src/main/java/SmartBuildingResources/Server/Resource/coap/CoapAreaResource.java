package SmartBuildingResources.Server.Resource.coap;

import SmartBuildingResources.Server.Resource.raw.AlarmActuator;
import SmartBuildingResources.Server.Resource.raw.AreaResource;
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

public class CoapAreaResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapAreaResource.class);

    private final static String OBJECT_TITLE = "Area";

    private AreaResource areaResource;


    private String nArea;

    private Integer nSensors=0;

    private Integer nPiano;

    private ObjectMapper objectMapper;


    public CoapAreaResource(String name, String nArea, AreaResource areaResource) throws InterruptedException {
        super(name);

        if (areaResource != null && nArea != null)
        {
            this.nArea = nArea;
            this.areaResource = areaResource;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", areaResource.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());
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
            senMLRecord.setBn(String.format("%s:%s", this.nArea, this.getName()));
            senMLRecord.setV(nSensors);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        } catch (Exception e) {
            return Optional.empty();
        }
    }
//
//    @Override
//    public void handleGET(CoapExchange exchange){
//
//        if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
//                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){
//
//            Optional<String> senmlPayload = getJsonSenmlResponse();
//
//            if(senmlPayload.isPresent())
//                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
//            else
//                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
//        }
//        //Otherwise respond with the default textplain payload
//        else
//            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(isActive), MediaTypeRegistry.TEXT_PLAIN);
//    }
//    @Override
//    public void handlePOST(CoapExchange exchange){
//        try{
//            //Empty request
//            if(exchange.getRequestPayload() == null){
//
//                //Update internal status
//                this.isActive = !isActive;
//                this.alarmActuator.setActive(isActive);
//
//                logger.info("Resource Status Updated: {}", this.isActive);
//
//                exchange.respond(CoAP.ResponseCode.CHANGED);
//            }
//            else
//                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
//
//        }catch (Exception e){
//            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
//            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
//        }
//    }
//    @Override
//    public void handlePUT(CoapExchange exchange){
//        try{
//
//            //If the request body is available
//            if(exchange.getRequestPayload() != null){
//
//                boolean submittedValue = Boolean.parseBoolean(new String(exchange.getRequestPayload()));
//
//                logger.info("Submitted value: {}", submittedValue);
//
//                //Update internal status
//                this.isActive = submittedValue;
//                this.alarmActuator.setActive(this.isActive);
//
//                logger.info("Resource Status Updated: {}", this.isActive);
//
//                exchange.respond(CoAP.ResponseCode.CHANGED);
//            }
//            else
//                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
//
//        }catch (Exception e){
//            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
//            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
//        }
//
//    }
}
