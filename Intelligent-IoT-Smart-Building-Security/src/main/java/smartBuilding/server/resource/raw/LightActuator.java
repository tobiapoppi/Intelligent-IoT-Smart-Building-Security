package smartBuilding.server.resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LightActuator extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(LightActuator.class);

    private static final String LOG_DISPLAY_NAME = "LightActuator";

    private static final String RESOURCE_TYPE = "iot.actuator.light";

    private Boolean isActive;

    public LightActuator() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.isActive = true;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
        notifyUpdate(isActive);
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.isActive;
    }


}