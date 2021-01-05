package smartBuilding.server.resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BuildingResourceRaw extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(BuildingResourceRaw.class);

    private static final String LOG_DISPLAY_NAME = "Building";

    private static final String RESOURCE_TYPE = "iot.building";

    public BuildingResourceRaw(){
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);

    }



    public BuildingResourceRaw(String nome) {
        super(nome, RESOURCE_TYPE);

    }

    @Override
    public Boolean loadUpdatedValue() {
        return null;
    }


}