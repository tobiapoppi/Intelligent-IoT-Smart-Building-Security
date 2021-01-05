package BuildingSecurityController.api.model;


public class GenericDeviceDescriptor {

    private String deviceId;

    public GenericDeviceDescriptor(){}

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public GenericDeviceDescriptor(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "GenericDeviceDescriptor{" +
                "deviceId='" + deviceId + '\'' +
                '}';
    }
}