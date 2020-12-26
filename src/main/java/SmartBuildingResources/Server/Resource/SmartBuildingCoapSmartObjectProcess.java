package SmartBuildingResources.Server.Resource;

import SmartBuildingResources.Server.Resource.coap.CoapAlarmResource;
import SmartBuildingResources.Server.Resource.coap.CoapCameraResource;
import SmartBuildingResources.Server.Resource.coap.CoapLightResource;
import SmartBuildingResources.Server.Resource.coap.CoapPirResource;
import SmartBuildingResources.Server.Resource.raw.*;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SmartBuildingCoapSmartObjectProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(SmartBuildingCoapSmartObjectProcess.class);

    public SmartBuildingCoapSmartObjectProcess() throws InterruptedException {

        super();
        String deviceId = String.format("%s", UUID.randomUUID().toString());




        this.add(CreateFloorResource(deviceId));




    }

    private CoapResource CreateFloorResource (String deviceId) throws InterruptedException {
        CoapResource FloorResource = new CoapResource("floor");
        this.add(CreateAreaResource(deviceId));

        return FloorResource;
    }
    private CoapResource CreateAreaResource (String deviceId) throws InterruptedException {

        CoapResource AreaResource = new CoapResource("area");
        LightActuator PMlightActuator = new LightActuator();
        CoapLightResource PMLightResource = new CoapLightResource("light", deviceId, PMlightActuator);

        AlarmActuator PMalarmActuator = new AlarmActuator();
        CoapAlarmResource PMalarmResource = new CoapAlarmResource("alarm", deviceId, PMalarmActuator);


        this.add(CreatePresenceMonitoringResource(deviceId));
        this.add(PMLightResource);
        this.add(PMalarmResource);

        return AreaResource;

    };

    private CoapResource CreatePresenceMonitoringResource(String deviceId) {
        CoapResource PresenceMonitoringResource = new CoapResource("presenceMonitoring");
        PirRawSensor PMPirRawSensor = new PirRawSensor();
        CameraRawSensor PMCameraRawSensor = new CameraRawSensor();
        CoapPirResource PMcoapPirResource = new CoapPirResource("pir", deviceId, PMPirRawSensor);
        CoapCameraResource PMcoapCameraRecource = new CoapCameraResource("camera", deviceId,PMCameraRawSensor );
        PresenceMonitoringResource.add(PMcoapPirResource);
        PresenceMonitoringResource.add(PMcoapCameraRecource);

        return PresenceMonitoringResource;


    };



    public static void main(String[] args) throws InterruptedException {

        SmartBuildingCoapSmartObjectProcess smartBuildingCoapSmartObjectProcess = new SmartBuildingCoapSmartObjectProcess();
        smartBuildingCoapSmartObjectProcess.start();

        logger.info("Coap Server Started ! Available resources: ");

        smartBuildingCoapSmartObjectProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });



    }


}

