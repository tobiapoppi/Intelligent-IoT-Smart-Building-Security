package BuildingSecurityController.api.model;

public class ResourceDescriptor {

    private String resourceId;
    private String deviceId;
    private String manufacturer;
    private String type;
    private String coreInterface;

    public ResourceDescriptor(){}

    public ResourceDescriptor(String resourceId, String deviceId, String manufacturer, String type, String coreInterface) {
        this.deviceId = deviceId;
        this.manufacturer = manufacturer;
        this.type = type;
        this.coreInterface = coreInterface;
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    public String getDeviceId(){
        return deviceId;
    }
    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCoreInterface() {
        return coreInterface;
    }

    public void setCoreInterface(String coreInterface) {
        this.coreInterface = coreInterface;
    }

    @Override
    public String toString() {
        return "ResourceDescriptor{" +
                "resourceId='" + resourceId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", type='" + type + '\'' +
                ", coreInterface='" + coreInterface + '\'' +
                '}';
    }
}
