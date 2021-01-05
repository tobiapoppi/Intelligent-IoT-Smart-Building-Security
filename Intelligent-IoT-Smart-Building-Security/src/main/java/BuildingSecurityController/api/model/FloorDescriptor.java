package BuildingSecurityController.api.model;

import java.util.List;

public class FloorDescriptor {
    private int number;
    private String floor_id;
    private List<String> areaList;

    public FloorDescriptor(int number, String floor_id, List<String> zoneList) {
        this.number = number;
        this.floor_id = floor_id;
        this.areaList = zoneList;
    }

    public FloorDescriptor(){

    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(String floor_id) {
        this.floor_id = floor_id;
    }

    public List<String> getAreaList() {
        return areaList;
    }

    public void setAreaList(List<String> areaList) {
        this.areaList = areaList;
    }

    @Override
    public String toString() {
        return "FloorDescriptor{" +
                "number=" + number +
                ", floor_id='" + floor_id + '\'' +
                ", zoneList=" + areaList +
                '}';
    }
}