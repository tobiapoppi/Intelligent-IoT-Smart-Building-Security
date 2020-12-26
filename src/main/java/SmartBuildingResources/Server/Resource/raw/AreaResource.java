package SmartBuildingResources.Server.Resource.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AreaResource extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(AreaResource.class);

    private static final String LOG_DISPLAY_NAME = "Area";

    private static final String RESOURCE_TYPE = "iot.area";

    private String nArea;

    private Integer nSensors=0;

    private Integer nPiano;

    public AreaResource(String nome, Integer nPiano) {
        super(nome, RESOURCE_TYPE);

        this.nArea= nome;
        this.nPiano=nPiano;

    }

    @Override
    public Boolean loadUpdatedValue() {
        return null;
    }


}