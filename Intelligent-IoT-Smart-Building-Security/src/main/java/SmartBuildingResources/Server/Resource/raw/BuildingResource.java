package SmartBuildingResources.Server.Resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BuildingResource extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(BuildingResource.class);

    private static final String LOG_DISPLAY_NAME = "Building";

    private static final String RESOURCE_TYPE = "iot.building";

    public BuildingResource(){
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);

    }



    public BuildingResource(String nome) {
        super(nome, RESOURCE_TYPE);

    }

    @Override
    public Boolean loadUpdatedValue() {
        return null;
    }


}