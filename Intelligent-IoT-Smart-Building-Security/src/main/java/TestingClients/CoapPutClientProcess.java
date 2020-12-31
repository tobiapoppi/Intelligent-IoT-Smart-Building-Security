package it.unimore.dipi.iot.client;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class CoapPutClientProcess {

	private final static Logger logger = LoggerFactory.getLogger(CoapPutClientProcess.class);

	private static final String COAP_ENDPOINT = "coap://127.0.0.1:5683/compressor/switch";

	public static void main(String[] args) {
		
		//Initialize coapClient
		CoapClient coapClient = new CoapClient(COAP_ENDPOINT);

		//Request Class is a generic CoAP message: in this case we want a PUT.
		//"Message ID", "Token" and other header's fields can be set 
		Request request = new Request(Code.PUT);

		//Set PUT request's payload
		String myPayload = "false";
		logger.info("PUT Request Random Payload: {}", myPayload);
		request.setPayload(myPayload);

		//Set Request as Confirmable
		request.setConfirmable(true);

		logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));

		//Synchronously send the POST request (blocking call)
		CoapResponse coapResp = null;

		try {

			coapResp = coapClient.advanced(request);

			//Pretty print for the received response
			logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

			//The "CoapResponse" message contains the response.
			String text = coapResp.getResponseText();
			logger.info("Payload: {}", text);
			logger.info("Message ID: " + coapResp.advanced().getMID());
			logger.info("Token: " + coapResp.advanced().getTokenString());

		} catch (ConnectorException | IOException e) {
			e.printStackTrace();
		}
	}
}