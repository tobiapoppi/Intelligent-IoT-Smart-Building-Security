package buildingSecurityController.api.data_transfer_object;


import java.util.List;

public class AreaUpdateRequest {
    private String area_id;
    private String floorId;
    private String areaName;

    public AreaUpdateRequest(){

    }

    public AreaUpdateRequest(String area_id, List<String> deviceIdList, String floorId, String areaName) {
        this.area_id = area_id;
        this.floorId = floorId;
        this.areaName = areaName;

    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
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