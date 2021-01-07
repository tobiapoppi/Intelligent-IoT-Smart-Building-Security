package BuildingSecurityController.api.model;

import java.util.ArrayList;
import java.util.List;

public class AreaDescriptor {
    private String areaId;
    private String areaName;
    private List<String> deviceIdList;
    private String floorId;

    public AreaDescriptor(){
        this.deviceIdList = new ArrayList<>();
    }

    public AreaDescriptor(String areaName, List<String> deviceIdList, String floorId, String areaId) {
        this.areaName = areaName;
        this.deviceIdList = deviceIdList;
        this.floorId = floorId;
        this.areaId = areaId;

    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<String> getDeviceIdList() {
        return deviceIdList;
    }

    public void addDeviceToList(String deviceId){
        this.deviceIdList.add(deviceId);
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}