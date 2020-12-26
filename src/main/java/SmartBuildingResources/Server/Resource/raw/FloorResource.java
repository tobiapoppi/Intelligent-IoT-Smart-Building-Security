package SmartBuildingResources.Server.Resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloorResource extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(FloorResource.class);

    private static final String LOG_DISPLAY_NAME = "Area";

    private static final String RESOURCE_TYPE = "iot.area";


    private Integer nAree;

    private Integer nPiano;

    public FloorResource(String nome) {
        super( nome, RESOURCE_TYPE);

    }

    @Override
    public Boolean loadUpdatedValue() {
        return null;
    }


}