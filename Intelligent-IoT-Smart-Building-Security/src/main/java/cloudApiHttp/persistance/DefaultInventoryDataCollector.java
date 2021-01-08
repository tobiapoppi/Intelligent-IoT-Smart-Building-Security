package cloudApiHttp.persistance;

import buildingSecurityController.api.auth.ExampleAuthenticator;
import buildingSecurityController.api.exception.IInventoryDataManagerConflict;
import buildingSecurityController.api.exception.IInventoryDataManagerException;
import buildingSecurityController.api.model.AreaDescriptor;
import buildingSecurityController.api.model.FloorDescriptor;
import buildingSecurityController.api.model.PolicyDescriptor;
import buildingSecurityController.api.model.UserDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SenMLPack;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class DefaultInventoryDataCollector implements IInventoryCollectorPack{

    final protected Logger logger = LoggerFactory.getLogger(DefaultInventoryDataCollector.class);


    @Override
    public SenMLPack createNewPack(SenMLPack pack) throws IInventoryDataManagerException, IInventoryDataManagerConflict {
        try {
            String record = new ObjectMapper().writeValueAsString(pack);

            FileWriter fileWriter = new FileWriter("recordSensorsFile", true);
            fileWriter.write(record);
            fileWriter.append("\n");
            fileWriter.flush();
            fileWriter.close();
            logger.info(record);

            return pack;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
