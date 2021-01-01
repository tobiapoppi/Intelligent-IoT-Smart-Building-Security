package SmartBuildingResources.Server.Resource;

import SmartBuildingResources.Server.Resource.coap.*;
import SmartBuildingResources.Server.Resource.raw.*;
import org.eclipse.californium.core.CoapResource;

public class SmartBuildingBaseResourceInitClient {

    public static CoapResource createFloorResource(String deviceId, Integer floorNumber) throws InterruptedException {

        FloorResource floorResource = new FloorResource();
        CoapFloorResource coapFloorResource = new CoapFloorResource(String.format("floor%s", floorNumber), floorResource);

        //CoapResource floorResource = new CoapResource(String.format("floor%s", floorNumber));
        coapFloorResource.add(createAreaResource(deviceId, "A"));
        return coapFloorResource;
    }

    private static CoapResource createAreaResource(String deviceId, String areaId) throws InterruptedException {

        AreaResource areaResource = new AreaResource();
        CoapAreaResource coapAreaResource = new CoapAreaResource(String.format("area%s", areaId), areaResource);

        LightActuator pmlightactuator = new LightActuator();
        CoapLightResource PMLightResource = new CoapLightResource("light", deviceId, pmlightactuator);

        AlarmActuator pmalarmactuator = new AlarmActuator();
        CoapAlarmResource PMalarmResource = new CoapAlarmResource("alarm", deviceId, pmalarmactuator);


        coapAreaResource.add(CreatePresenceMonitoringResource(deviceId));
        coapAreaResource.add(PMLightResource);
        coapAreaResource.add(PMalarmResource);

        return coapAreaResource;

    };

    private static CoapResource CreatePresenceMonitoringResource(String deviceId) {
        CoapResource PresenceMonitoringResource = new CoapResource("presenceMonitoring");
        PirRawSensor PMPirRawSensor = new PirRawSensor();
        CameraRawSensor PMCameraRawSensor = new CameraRawSensor();
        CoapPirResource PMcoapPirResource = new CoapPirResource("pir", deviceId, PMPirRawSensor);
        CoapCameraResource PMcoapCameraRecource = new CoapCameraResource("camera", deviceId,PMCameraRawSensor );

        PresenceMonitoringResource.add(PMcoapPirResource);
        PresenceMonitoringResource.add(PMcoapCameraRecource);

        return PresenceMonitoringResource;
    };
}
