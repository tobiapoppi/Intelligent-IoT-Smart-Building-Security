package BuildingSecurityController.api.client;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LookupAndObserveProcess {

    private final static Logger logger = LoggerFactory.getLogger(LookupAndObserveProcess.class);

    private static final String COAP_ENDPOINT = "coap://127.0.0.1:<porta>/rd-lookup/res";
    //private static final String COAP_ENDPOINT = "coap://<futuro_ip_del_raspberry>:<porta>/rd-lookup/res";

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

            logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResponse));

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
