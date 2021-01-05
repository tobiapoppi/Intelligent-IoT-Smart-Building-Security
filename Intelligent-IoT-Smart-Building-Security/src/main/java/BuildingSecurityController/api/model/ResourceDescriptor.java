package BuildingSecurityController.api.model;

public class ResourceDescriptor {

    private String deviceId;
    private String manufacturer;
    private String type;
    private String coreInterface;

    public ResourceDescriptor(){}

    public ResourceDescriptor(String resourceId, String manufacturer, String type, String coreInterface) {
        this.deviceId = resourceId;
        this.manufacturer = manufacturer;
        this.type = type;
        this.coreInterface = coreInterface;
    }

    public String getResourceId() {
        return deviceId;
    }

    public void setResourceId(String resourceId) {
        this.deviceId = resourceId;
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
                "resourceId='" + deviceId + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", type='" + type + '\'' +
                ", coreInterface='" + coreInterface + '\'' +
                '}';
    }
}
