package smartBuilding.server.resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class FloorResource extends SmartObjectResource<String> {

    private static Logger logger = LoggerFactory.getLogger(FloorResource.class);

    private static final String LOG_DISPLAY_NAME = "Floor";

    private static final String RESOURCE_TYPE = "iot.floor";

    public FloorResource(String nome) {
        super (nome, RESOURCE_TYPE);

    }
    public FloorResource() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
    }

    @Override
    public String loadUpdatedValue() {
        return null;
    }


}