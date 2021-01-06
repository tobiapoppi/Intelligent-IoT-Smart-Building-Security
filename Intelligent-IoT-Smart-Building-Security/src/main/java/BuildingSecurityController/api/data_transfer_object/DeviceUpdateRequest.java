package BuildingSecurityController.api.data_transfer_object;

import java.util.*;

public class DeviceUpdateRequest {

    private String areaId;

    private String floorId;

    private String deviceId;

    public DeviceUpdateRequest(){}

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public DeviceUpdateRequest(String areaId, String floorId, String deviceId) {
        this.areaId = areaId;
        this.floorId = floorId;
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "GenericDeviceDescriptor{" +
                "areaId='" + areaId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }

}