package utils;


import BuildingSecurityController.api.model.AreaDescriptor;
import BuildingSecurityController.api.model.FloorDescriptor;
import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.model.UserDescriptor;
import BuildingSecurityController.api.persistance.IInventoryDataManager;

public class DummyDataGenerator {

    public static void generateRandomPolicies(IInventoryDataManager inventoryDataManager){

        try{

            PolicyDescriptor pol1 = new PolicyDescriptor();

            pol1.setPolicy_id("0001");
            pol1.setIs_enabled(false);
            pol1.setArea_id("1:B");
            pol1.setStart_working_time("19:00");
            pol1.setEnd_working_time("6:00");
            pol1.setPresence_mode(true);

            PolicyDescriptor pol2 = new PolicyDescriptor();

            pol2.setPolicy_id("0002");
            pol2.setIs_enabled(true);
            pol2.setArea_id("1:B");
            pol2.setStart_working_time("19:00");
            pol2.setEnd_working_time("6:00");
            pol2.setPresence_mode(false);
            pol2.setMax_persons(6);

            PolicyDescriptor pol3 = new PolicyDescriptor();

            pol3.setPolicy_id("0003");
            pol3.setIs_enabled(true);
            pol3.setArea_id("0:A");
            pol3.setStart_working_time("9:00");
            pol3.setEnd_working_time("15:00");
            pol3.setPresence_mode(true);

            inventoryDataManager.createNewPolicy(pol1);
            inventoryDataManager.createNewPolicy(pol2);
            inventoryDataManager.createNewPolicy(pol3);

            UserDescriptor user1 = new UserDescriptor();

            user1.setUsername("user");
            user1.setPassword("pass");
            user1.setEmail("test");
            user1.setRole("USER");

            inventoryDataManager.createNewUser(user1);

            FloorDescriptor floor1 = new FloorDescriptor();

            floor1.setFloor_id("1");

            FloorDescriptor floor0 = new FloorDescriptor();

            floor0.setFloor_id("0");
            FloorDescriptor floor2 = new FloorDescriptor();

            floor2.setFloor_id("2");

            FloorDescriptor floor3 = new FloorDescriptor();

            floor3.setFloor_id("3");
            FloorDescriptor floor4 = new FloorDescriptor();

            floor4.setFloor_id("4");


            inventoryDataManager.createNewFloor(floor0);
            inventoryDataManager.createNewFloor(floor1);
            inventoryDataManager.createNewFloor(floor2);
            inventoryDataManager.createNewFloor(floor3);
            inventoryDataManager.createNewFloor(floor4);


            AreaDescriptor area0 = new AreaDescriptor();
            AreaDescriptor area1 = new AreaDescriptor();
            AreaDescriptor area2 = new AreaDescriptor();
            AreaDescriptor area3 = new AreaDescriptor();
            AreaDescriptor area4 = new AreaDescriptor();
            AreaDescriptor area5 = new AreaDescriptor();
            AreaDescriptor area6 = new AreaDescriptor();
            AreaDescriptor area7 = new AreaDescriptor();
            AreaDescriptor area8 = new AreaDescriptor();
            AreaDescriptor area9 = new AreaDescriptor();
            AreaDescriptor area10 = new AreaDescriptor();
            AreaDescriptor area11 = new AreaDescriptor();

            area0.setAreaName("A");
            area0.setFloorId("0");
            area1.setAreaName("B");
            area1.setFloorId("0");
            area2.setAreaName("C");
            area2.setFloorId("0");
            area3.setAreaName("A");
            area3.setFloorId("1");
            area4.setAreaName("B");
            area4.setFloorId("1");
            area5.setAreaName("C");
            area5.setFloorId("1");
            area6.setAreaName("A");
            area6.setFloorId("2");
            area7.setAreaName("B");
            area7.setFloorId("2");
            area8.setAreaName("A");
            area8.setFloorId("3");
            area9.setAreaName("B");
            area9.setFloorId("3");
            area10.setAreaName("A");
            area10.setFloorId("4");
            area11.setAreaName("B");
            area11.setFloorId("4");

            inventoryDataManager.createNewArea(area0);
            inventoryDataManager.createNewArea(area1);
            inventoryDataManager.createNewArea(area2);
            inventoryDataManager.createNewArea(area3);
            inventoryDataManager.createNewArea(area4);
            inventoryDataManager.createNewArea(area5);
            inventoryDataManager.createNewArea(area6);
            inventoryDataManager.createNewArea(area7);
            inventoryDataManager.createNewArea(area8);
            inventoryDataManager.createNewArea(area9);
            inventoryDataManager.createNewArea(area10);
            inventoryDataManager.createNewArea(area11);



        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
