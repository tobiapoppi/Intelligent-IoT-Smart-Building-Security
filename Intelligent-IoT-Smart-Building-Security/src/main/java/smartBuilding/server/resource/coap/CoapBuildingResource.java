package smartBuilding.server.resource.coap;

import smartBuilding.server.resource.SmartBuildingCoapSmartObjectProcess;
import smartBuilding.server.resource.raw.BuildingResourceRaw;
import smartBuilding.server.resource.raw.FloorResource;
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

public class CoapBuildingResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapBuildingResource.class);

    private final static String OBJECT_TITLE = "Building";

    private static final String TARGET_LISTENING_IP = "192.168.1.107";

    private static final int TARGET_PORT = 5683;

    private BuildingResourceRaw buildingResourceRaw;

    private ObjectMapper objectMapper;


    public CoapBuildingResource(String name, BuildingResourceRaw buildingResourceRaw) throws InterruptedException {
        super(name);

        if (buildingResourceRaw != null)
        {
            this.buildingResourceRaw = buildingResourceRaw;
            this.objectMapper=new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", buildingResourceRaw.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_B.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_LINK_FORMAT));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));


        }
        else {
            logger.error( "ERROR -->NULL Raw References" );
        }

    }

    private CoapResource createFloorResource(String floorId) throws InterruptedException {

        FloorResource floorResource = new FloorResource();
        CoapFloorResource coapFloorResource = new CoapFloorResource(String.format("floor%s", floorId), floorResource);
        return coapFloorResource;
    };

    private Optional<String> getJsonSenmlResponse() {

        try {

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s", this.getName()));
            senMLPack.add(senMLRecord);

            logger.info("{}", senMLPack);

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

            if(senmlPayload.isPresent()){
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            }
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public void handlePOST(CoapExchange exchange) {
        try {
                if (exchange.getRequestPayload() != null) {
                    String floorId = new String(exchange.getRequestPayload());

                    this.add(createFloorResource(floorId));


                    logger.info("Resource Status Updated: Floor {} Created", floorId);

                    exchange.respond(CoAP.ResponseCode.CREATED);

                    SmartBuildingCoapSmartObjectProcess.registerToCoapResourceDirectory(this.getParent(), "CoapEndpointSmartObject", TARGET_LISTENING_IP, TARGET_PORT);

                } else
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

            } catch (Exception e) {
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

                logger.info("Resource Status Updated: Building {} deleted", this.getName());

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
