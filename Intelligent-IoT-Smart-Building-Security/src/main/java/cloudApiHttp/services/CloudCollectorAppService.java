package cloudApiHttp.services;

import cloudApiHttp.resources.PackResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class CloudCollectorAppService extends Application<CloudAppConfig> {

    private static final Logger logger = LoggerFactory.getLogger(CloudCollectorAppService.class);

    public static void main(String[] args) throws Exception {



        new CloudCollectorAppService().run(new String[]{"server", args.length > 0 ? args[0] : "configurationCloud.yml"});



    }


    public void run(CloudAppConfig cloudAppConfig, Environment environment) throws Exception {

        environment.jersey().register(new PackResource(cloudAppConfig));

        // Enable CORS headers
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "POST");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

    }

    @Override
    public void initialize(Bootstrap<CloudAppConfig> bootstrap) {


        bootstrap.addBundle(new SwaggerBundle<CloudAppConfig>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(CloudAppConfig configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }


}
