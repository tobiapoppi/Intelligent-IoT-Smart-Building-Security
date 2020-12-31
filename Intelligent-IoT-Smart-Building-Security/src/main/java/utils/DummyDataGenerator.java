package utils;

import BuildingSecurityController.api.auth.User;
import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.model.UserDescriptor;
import BuildingSecurityController.api.persistance.IInventoryDataManager;

public class DummyDataGenerator {

    public static void generateRandomPolicies(IInventoryDataManager inventoryDataManager){

        try{

            PolicyDescriptor pol1 = new PolicyDescriptor();

            pol1.setPolicy_id("0001");
            pol1.setIs_enabled(false);
            pol1.setLocation_id("zona1");
            pol1.setStart_working_time("19.00");
            pol1.setEnd_working_time("6.00");
            pol1.setPresence_mode(true);

            PolicyDescriptor pol2 = new PolicyDescriptor();

            pol2.setPolicy_id("0002");
            pol2.setIs_enabled(true);
            pol2.setLocation_id("zona1");
            pol2.setStart_working_time("19.00");
            pol2.setEnd_working_time("6.00");
            pol2.setPresence_mode(false);
            pol2.setMax_persons(6);

            PolicyDescriptor pol3 = new PolicyDescriptor();

            pol3.setPolicy_id("0003");
            pol3.setIs_enabled(true);
            pol3.setLocation_id("zona5");
            pol3.setStart_working_time("22.00");
            pol3.setEnd_working_time("10.00");
            pol3.setPresence_mode(true);

            inventoryDataManager.createNewPolicy(pol1);
            inventoryDataManager.createNewPolicy(pol2);
            inventoryDataManager.createNewPolicy(pol3);

            UserDescriptor user1 = new UserDescriptor();

            user1.setUsername("user");
            user1.setPassword("pass");
            user1.setEmail("test");
            user1.setRole("USER");

            UserDescriptor user2 = new UserDescriptor();

            user2.setUsername("tobi");
            user2.setPassword("tobi");
            user2.setEmail("tessst");
            user2.setRole("ADMIN");

            inventoryDataManager.createNewUser(user1);
            inventoryDataManager.createNewUser(user2);


        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
