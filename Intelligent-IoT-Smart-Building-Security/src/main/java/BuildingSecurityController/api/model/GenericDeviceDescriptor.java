package BuildingSecurityController.api.model;

import java.util.*;

public class GenericDeviceDescriptor {

    private String areaId;

    private String deviceId;

    private List<String> resourceList;

    public GenericDeviceDescriptor(){}

    public List<String> getResourceList(){
        return resourceList;
    }

    public void setResourceList(List<String> resourceList){
        this.resourceList = resourceList;
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

    public GenericDeviceDescriptor(String deviceId, String areaId, List<String> resourceList) {
        this.deviceId = deviceId;
        this.areaId = areaId;
        this.resourceList = resourceList;
    }

    @Override
    public String toString() {
        return "GenericDeviceDescriptor{" +
                "deviceId='" + deviceId + '\'' +
                '}';
    }
}