package SmartBuildingResources.Server.Resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PirRawSensor extends SmartObjectResource<Boolean> {


    private static Logger logger = LoggerFactory.getLogger(PirRawSensor.class);


    private static final boolean value = false;

    private static final double half = 0.7;

    private static final String LOG_DISPLAY_NAME = "Pir Sensor";

    //Ms associated to data update
    public static final long UPDATE_PERIOD = 5000;

    private static final long TASK_DELAY_TIME = 10000;

    private static final String RESOURCE_TYPE = "iot.sensor.pir";

    private Boolean updatedValue;

    private String location_id;

    private Random random;


    private Timer updateTimer = null;


    public PirRawSensor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    private void init() {

        try {
            this.random = new Random(System.currentTimeMillis());

            if (half > this.random.nextDouble()) {

                this.updatedValue = Boolean.TRUE;
            } else {
                this.updatedValue = Boolean.FALSE;
            }


            startPeriodicEventValueUpdateTask();

        } catch (Exception e) {
            logger.error("Error initializing the IoT Resource ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask() {

        try {

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {


                    if (half > random.nextDouble()) {

                        updatedValue = Boolean.TRUE;
                    } else {
                        updatedValue = Boolean.FALSE;
                    }
                    notifyUpdate(updatedValue);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        } catch (Exception e) {
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.updatedValue;
    }

    public static void main(String[] args) {

        PirRawSensor rawResource = new PirRawSensor();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                rawResource.getType(),
                rawResource.getId(),
                LOG_DISPLAY_NAME,
                rawResource.loadUpdatedValue());

        rawResource.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {

                if (resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }
}
