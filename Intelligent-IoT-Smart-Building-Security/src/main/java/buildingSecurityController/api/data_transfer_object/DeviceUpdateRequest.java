package buildingSecurityController.api.data_transfer_object;

public class DeviceUpdateRequest {

    private String areaId;

    private String deviceId;

    public DeviceUpdateRequest(){}



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

    public DeviceUpdateRequest(String areaId, String deviceId) {
        this.areaId = areaId;
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