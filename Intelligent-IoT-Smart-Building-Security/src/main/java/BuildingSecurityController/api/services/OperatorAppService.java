package BuildingSecurityController.api.services;


import BuildingSecurityController.api.auth.ExampleAuthorizer;
import BuildingSecurityController.api.auth.ExampleAuthenticator;
import BuildingSecurityController.api.auth.User;
import BuildingSecurityController.api.client.LookupAndObserveProcess;
import BuildingSecurityController.api.resources.BuildingResource;
import BuildingSecurityController.api.resources.PolicyResource;
import BuildingSecurityController.api.resources.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DummyDataGenerator;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumSet;

public class OperatorAppService extends Application<OperatorAppConfig> {

    private static final Logger logger = LoggerFactory.getLogger(OperatorAppService.class);

    public static void main(String[] args) throws Exception {

        LookupAndObserveProcess lookupAndObserveProcess = new LookupAndObserveProcess();

        new OperatorAppService().run(new String[]{"server", args.length > 0 ? args[0] : "configuration.yml"});

        Thread newThread = new Thread(() -> {
            lookupAndObserveProcess.run();
        });
        newThread.start();

    }


    public void run(OperatorAppConfig operatorAppConfig, Environment environment) throws Exception {

        //creo dati fittizzi per le risorse
        DummyDataGenerator.generateRandomPolicies(operatorAppConfig.getInventoryDataManager());



        //registro le risorse per l'autenticazione all'api

        environment.jersey().register(new AuthDynamicFeature(
                        new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(new ExampleAuthenticator())
                        .setAuthorizer(new ExampleAuthorizer())
                        .setRealm("SUPER SECRET STUFF")
                        .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));



        environment.jersey().register(new BuildingResource(operatorAppConfig));
        environment.jersey().register(new UserResource(operatorAppConfig));
        environment.jersey().register(new PolicyResource(operatorAppConfig));


        // Enable CORS headers
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    @Override
    public void initialize(Bootstrap<OperatorAppConfig> bootstrap) {


        bootstrap.addBundle(new SwaggerBundle<OperatorAppConfig>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(OperatorAppConfig configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }
}
