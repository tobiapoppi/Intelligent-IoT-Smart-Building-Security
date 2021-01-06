package BuildingSecurityController.api.model;

import java.util.*;

public class GenericDeviceDescriptor {

    private String areaId;

    private String floorId;

    private String deviceId;

    private List<String> resourceList;

    public GenericDeviceDescriptor(){
        resourceList = new ArrayList<>();
        areaId = "unallocated";
        floorId = "unallocated";
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

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

    public GenericDeviceDescriptor(String areaId, String floorId, String deviceId, List<String> resourceList) {
        this.areaId = areaId;
        this.floorId = floorId;
        this.deviceId = deviceId;
        this.resourceList = resourceList;
    }

    @Override
    public String toString() {
        return "GenericDeviceDescriptor{" +
                "areaId='" + areaId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", resourceList=" + resourceList +
                '}';
    }

    public void addValueToResourceList(String value){
        this.resourceList.add(value);
    }

}