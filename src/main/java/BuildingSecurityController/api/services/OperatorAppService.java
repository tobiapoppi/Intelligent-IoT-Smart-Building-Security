package BuildingSecurityController.api.services;


import BuildingSecurityController.api.auth.ExampleAuthorizer;
import BuildingSecurityController.api.auth.ExampleAuthenticator;
import BuildingSecurityController.api.auth.User;
import BuildingSecurityController.api.resources.PolicyResource;
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
import java.util.EnumSet;

public class OperatorAppService extends Application<OperatorAppConfig> {

    public static void main(String[] args) throws Exception {

        new OperatorAppService().run(new String[]{"server", args.length > 0 ? args[0] : "configuration.yml"});

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
