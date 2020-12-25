package SmartBuildingResources.Server.Resource;

import SmartBuildingResources.Server.Resource.coap.CoapPirResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class HvacCoapSmartObjectProcess  extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(HvacCoapSmartObjectProcess.class);

    public HvacCoapSmartObjectProcess() {

        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());



        CoapPirResource coapPirResource = new CoapPirResource(deviceId, "Pir",)
    }

}
