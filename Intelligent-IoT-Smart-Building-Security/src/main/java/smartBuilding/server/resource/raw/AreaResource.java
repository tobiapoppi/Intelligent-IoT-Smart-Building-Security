package smartBuilding.server.resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AreaResource extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(AreaResource.class);

    private static final String LOG_DISPLAY_NAME = "Area";

    private static final String RESOURCE_TYPE = "iot.area";

    public AreaResource(){
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);

    }



    public AreaResource(String nome) {
        super(nome, RESOURCE_TYPE);

    }

    @Override
    public Boolean loadUpdatedValue() {
        return null;
    }


}