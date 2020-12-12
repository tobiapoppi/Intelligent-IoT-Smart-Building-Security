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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LookupAndObserveProcess {

    private final static Logger logger = LoggerFactory.getLogger(LookupAndObserveProcess.class);

    private static final String COAP_ENDPOINT = "coap://127.0.0.1:<porta>/rd-lookup/res";
    //private static final String COAP_ENDPOINT = "coap://<futuro_ip_del_raspberry>:<porta>/rd-lookup/res";

    private static final String OBSERVABLE_CORE_ATTRIBUTE = "obs";
    private static final String INTERFACE_CORE_ATTRIBUTE = "if";
    private static final String WELL_KNOWN_CORE_URI = "/.well-known/core";

    private static List<String> targetObservableResList = null;
    private static Map<String, CoapObserveRelation> observingRelationMap = null;

    public static void main(String[] args){

        CoapClient coapClient = new CoapClient(COAP_ENDPOINT);

        //we set the type of request as a "Get"
        Request request = new Request(Code.GET);
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


                        //I DUE IF SONO SBAGLIATI, DA SISTEMARE
                        //TODO
                        //if(webLink.getURI() != null && !webLink.getURI().equals(WELL_KNOWN_CORE_URI) && webLink.getAttributes() != null && webLink.getAttributes().getAttributeValues(INTERFACE_CORE_ATTRIBUTE).get(0).equals(CoreInterfaces.CORE_A.getValue())){

                            //if (webLink.getAttributes().containsAttribute(OBSERVABLE_CORE_ATTRIBUTE) &&
                                    webLink.getAttributes().containsAttribute(INTERFACE_CORE_ATTRIBUTE) &&
                                    (webLink.getAttributes().getAttributeValues(INTERFACE_CORE_ATTRIBUTE).contains(CoreInterfaces.CORE_S.getValue())))

                        }

                    });

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

    public void startObservingResources (){

    }





}
