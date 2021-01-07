package BuildingSecurityController.api.model;

import java.util.List;

public class FloorDescriptor {
    private String floor_id;

    public FloorDescriptor( String floor_id) {
        this.floor_id = floor_id;
    }

    public FloorDescriptor(){

    }

    public String getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(String floor_id) {
        this.floor_id = floor_id;
    }


    @Override
    public String toString() {
        return "FloorDescriptor{" +
                ", floor_id='" + floor_id + '\'' +
                '}';
    }
}