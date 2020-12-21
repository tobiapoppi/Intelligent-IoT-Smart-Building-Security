package BuildingSecurityController.api.client;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CoreInterfaces;

import java.io.IOException;
import java.util.*;

public class LookupAndObserveProcess {

    private final static Logger logger = LoggerFactory.getLogger(LookupAndObserveProcess.class);

    //private static final String COAP_ENDPOINT = "coap://127.0.0.1:<porta>/rd-lookup/res";

    private static final String TARGET_RD_IP = "127.0.0.1";
    private static final int TARGET_RD_PORT = 9999;
    private static final String RD_LOOKUP_URI = "/rd-lookup/res";

    private static final String OBSERVABLE_CORE_ATTRIBUTE = "obs";
    private static final String INTERFACE_CORE_ATTRIBUTE = "if";
    private static final String WELL_KNOWN_CORE_URI = "/.well-known/core";

    private static List<String> targetObservableResList = null;
    private static Map<String, CoapObserveRelation> observingRelationMap = null;

    public static void main(String[] args){

        //init coap client
        CoapClient coapClient = new CoapClient();

        //init target resource list array and observing relations
        targetObservableResList = new ArrayList<>();
        observingRelationMap = new HashMap<>();

        //discover all available sensors and actuators
        lookupTarget(coapClient);

        //start observing resources
        targetObservableResList.forEach(targetResourceUrl -> {
            startObservingResources(coapClient, targetResourceUrl);
        });

    }

    private static void lookupTarget(CoapClient coapClient){

        //we set the type of request as a "Get"
        Request request = new Request(Code.GET);
        request.setURI(String.format("coap://%s:%d%s", TARGET_RD_IP, TARGET_RD_PORT, RD_LOOKUP_URI));
        request.setConfirmable(true);

        logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));



        //the system is synchronous, will wait the response (blocking)
        CoapResponse coapResponse = null;

        try{
            coapResponse = coapClient.advanced(request);


            //la risposta non deve essere nulla
            if(coapResponse != null){

                logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResponse));

                //la risposta non deve essere di un media type che non sia CORE LINK FORMAT
                if (coapResponse.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){

                    Set<WebLink> links = LinkFormat.parse(coapResponse.getResponseText());

                    links.forEach(webLink -> {

                        if(webLink.getURI() != null && !webLink.getURI().equals(WELL_KNOWN_CORE_URI) && webLink.getAttributes() != null && webLink.getAttributes().getCount() > 0){

                            if (webLink.getAttributes().containsAttribute(OBSERVABLE_CORE_ATTRIBUTE) &&
                                    webLink.getAttributes().containsAttribute(INTERFACE_CORE_ATTRIBUTE) &&
                                    (webLink.getAttributes().getAttributeValues(INTERFACE_CORE_ATTRIBUTE)
                                            .contains(CoreInterfaces.CORE_S.getValue()) || webLink.getAttributes()
                                            .getAttributeValues(INTERFACE_CORE_ATTRIBUTE).contains(CoreInterfaces.CORE_A.getValue()))){

                                logger.info("Target Resource found! URI: {}", webLink.getURI());

                                String targetResourceUrl = String.format("coap://%s:%d%s", TARGET_RD_IP, TARGET_RD_PORT, webLink.getURI());

                                targetObservableResList.add(targetResourceUrl);
                                logger.info("Target Resource URL: {} correctly saved!", targetResourceUrl);

                            }
                            else{
                                logger.info("Resource {} does not match filtering parameters.", webLink.getURI());
                            }

                        }

                    });

                }
                else{
                    logger.info("Core Link Format Response not found.");
                }

            }



            String text = coapResponse.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResponse.advanced().getMID());
            logger.info("Token: " + coapResponse.advanced().getTokenString());



            ///  HERE WE NEED A CODE TO PARSE ALL THE RES RECEIVED, LOOK WHICH WE NEED TO OBS AND ADD THEM TO HE OBSERVINGLIST ///




        }catch (ConnectorException | IOException e){
            e.printStackTrace();
        }
    }

    private static void startObservingResources (CoapClient coapClient, String targetUrl){

        logger.info("OBSERVING ... {}", targetUrl);
        Request request = Request.newGet().setURI(targetUrl).setObserve();
        request.setConfirmable(true);
        CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);
            }

            @Override
            public void onError() {
                logger.error("OBSERVE {} FAILED", targetUrl);
            }
        });

        observingRelationMap.put(targetUrl, relation);

    }



}
