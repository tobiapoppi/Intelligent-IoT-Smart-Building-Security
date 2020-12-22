package BuildingSecurityController.api.model;

import java.util.List;

public class FloorDescriptor {
    private int number;
    private String floor_id;
    private List<String> zoneList;

    public FloorDescriptor(int number, String floor_id, List<String> zoneList) {
        this.number = number;
        this.floor_id = floor_id;
        this.zoneList = zoneList;
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

    public List<String> getZoneList() {
        return zoneList;
    }

    public void setZoneList(List<String> zoneList) {
        this.zoneList = zoneList;
    }

    @Override
    public String toString() {
        return "FloorDescriptor{" +
                "number=" + number +
                ", floor_id='" + floor_id + '\'' +
                ", zoneList=" + zoneList +
                '}';
    }
}


