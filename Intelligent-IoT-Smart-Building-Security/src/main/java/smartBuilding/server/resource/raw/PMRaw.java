package smartBuilding.server.resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PMRaw extends SmartObjectResource<Integer>{

    private static Logger logger = LoggerFactory.getLogger(PMRaw.class);

    private static final String LOG_DISPLAY_NAME = "PresenceMonitoring";


    private static final String RESOURCE_TYPE = "iot.sensor.presencemonitoring";



    private void init(){

    }


    @Override
    public Integer loadUpdatedValue() {
        return null;
    }
}
