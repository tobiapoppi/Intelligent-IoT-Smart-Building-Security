package BuildingSecurityController.api.model;

import java.util.ArrayList;
import java.util.List;

public class AreaDescriptor {
    private String area_id;
    private List<String> deviceIdList;
    private String floorId;

    public AreaDescriptor(){

        this.deviceIdList = new ArrayList<>();
    }

    public AreaDescriptor(String area_id, List<String> deviceIdList, String floorId) {
        this.area_id = area_id;
        this.deviceIdList = deviceIdList;
        this.floorId = floorId;


    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
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

    @Override
    public String toString() {
        return super.toString();
    }
}