package SmartBuildingResources.Server.Resource.coap;

import SmartBuildingResources.Server.Resource.raw.AreaResource;
import SmartBuildingResources.Server.Resource.raw.FloorResource;
import SmartBuildingResources.Server.Resource.raw.PresenceMonitoringResource;
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

public class CoapPresenceMonitoringResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapPresenceMonitoringResource.class);

    private final static String OBJECT_TITLE = "PresenceMonitoring";

    private PresenceMonitoringResource presenceMonitoringResource;

    private ObjectMapper objectMapper;




    public CoapPresenceMonitoringResource(String name, PresenceMonitoringResource presenceMonitoringResource) throws InterruptedException {
        super(name);

        if (presenceMonitoringResource != null)
        {
            this.presenceMonitoringResource = presenceMonitoringResource;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", presenceMonitoringResource.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_LL.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_LINK_FORMAT));

        }
        else {
            logger.error( "ERROR -->NULL Raw References" );
        }

    }

    private CoapResource createAreaResource(String areaId) throws InterruptedException {

        AreaResource areaResource = new AreaResource();
        CoapAreaResource coapAreaResource = new CoapAreaResource(String.format("area%s", areaId), areaResource);
        return coapAreaResource;
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
    public void handlePOST(CoapExchange exchange){
        try{
            //Empty request
   //         if(exchange.getRequestPayload() == (int)){
                String areaId = new String(exchange.getRequestPayload());

                this.add(createAreaResource(areaId));


                logger.info("Resource Status Updated: Area {} Created", areaId);

                exchange.respond(CoAP.ResponseCode.CHANGED);
   //         }
   //         else
   //             exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

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

                String submittedValue = new String(exchange.getRequestPayload());

                //Update internal status
                this.setName(submittedValue);

                logger.info("Resource Status Updated: {}", submittedValue);

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
    public void handleDELETE(CoapExchange exchange){
        try{

            //If the request body is available
            if(exchange.getRequestPayload() != null){

                String deletedValue = new String(exchange.getRequestPayload());

                //Update internal status
                this.delete(deletedValue);

                logger.info("Resource Status Updated: Area {} deleted", deletedValue);

                exchange.respond(CoAP.ResponseCode.CHANGED);
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling DELETE -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }
}
