package SmartBuildingResources.Server.Resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PresenceMonitoringResource extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(PresenceMonitoringResource.class);

    private static final String LOG_DISPLAY_NAME = "PresenceMonitoring";

    private static final String RESOURCE_TYPE = "iot.presencemonitoring";

    public PresenceMonitoringResource(){
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);

    }



    public PresenceMonitoringResource(String nome) {
        super(nome, RESOURCE_TYPE);

    }

    @Override
    public Boolean loadUpdatedValue() {
        return null;
    }


}