package BuildingSecurityController.api.client;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

public class CoapResourceClient {

    private final static Logger logger = LoggerFactory.getLogger(CoapResourceClient.class);

    private static final String SMARTOBJECT_ENDPOINT = "coap://192.168.1.107:5683/buildingPoppiZaniboniInc";

    public CoapResourceClient(){

    }

    public static CoapResponse getRequest(String uriRequest){
        CoapClient coapClient = new CoapClient(String.format("%s/%s", SMARTOBJECT_ENDPOINT, uriRequest));
        Request request = new Request(CoAP.Code.GET);
        request.setConfirmable(true);
        request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

        CoapResponse coapResponse = null;
        try{

            coapResponse = coapClient.advanced(request);
            return coapResponse;

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CoapResponse postRequest(String uriRequest, String payload){
        CoapClient coapClient = new CoapClient(String.format("%s%s", SMARTOBJECT_ENDPOINT, uriRequest));
        Request request = new Request(CoAP.Code.POST);
        request.setConfirmable(true);
        request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        request.setPayload(payload);
        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

        CoapResponse coapResponse = null;
        try{
            coapResponse = coapClient.advanced(request);
            return coapResponse;

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CoapResponse putRequest(String uriRequest, String payload){
        CoapClient coapClient = new CoapClient(String.format("%s/%s", SMARTOBJECT_ENDPOINT, uriRequest));
        Request request = new Request(CoAP.Code.PUT);
        request.setConfirmable(true);
        request.setPayload(payload);
        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

        CoapResponse coapResponse = null;
        try{
            coapResponse = coapClient.advanced(request);
            logger.info("Response Pretty Print:\n{}", Utils.prettyPrint(coapResponse));
            return coapResponse;
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CoapResponse deleteRequest(String uriRequest){
        CoapClient coapClient = new CoapClient(String.format("%s/%s", SMARTOBJECT_ENDPOINT, uriRequest));
        Request request = new Request(CoAP.Code.DELETE);
        request.setConfirmable(true);
        request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

        CoapResponse coapResponse = null;
        try{
            coapResponse = coapClient.advanced(request);
            return coapResponse;
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
