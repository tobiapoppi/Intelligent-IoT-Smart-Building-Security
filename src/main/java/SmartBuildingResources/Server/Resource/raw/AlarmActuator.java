package SmartBuildingResources.Server.Resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AlarmActuator extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(AlarmActuator.class);

    private static final String LOG_DISPLAY_NAME = "AlarmActuator";

    private static final String RESOURCE_TYPE = "iot.actuator.alarm";

    private Boolean isActive;

    private String location_id;

    public AlarmActuator() {
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

    public static void main(String[] args) {

        AlarmActuator rawResource = new AlarmActuator();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                rawResource.getType(),
                rawResource.getId(),
                LOG_DISPLAY_NAME,
                rawResource.loadUpdatedValue());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<100; i++){
                        rawResource.setActive(!rawResource.loadUpdatedValue());
                        Thread.sleep(1000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        rawResource.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

}