package BuildingSecurityController.api.resources;

import BuildingSecurityController.api.client.CoapResourceClient;
import BuildingSecurityController.api.client.LookupAndObserveProcess;
import BuildingSecurityController.api.data_transfer_object.FloorCreationRequest;
import BuildingSecurityController.api.data_transfer_object.FloorUpdateRequest;
import BuildingSecurityController.api.data_transfer_object.PolicyCreationRequest;
import BuildingSecurityController.api.data_transfer_object.PolicyUpdateRequest;
import BuildingSecurityController.api.exception.IInventoryDataManagerConflict;
import BuildingSecurityController.api.model.FloorDescriptor;
import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.services.OperatorAppConfig;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/buildingPoppiZaniboniInc/floor")
@Api("IoT Building Resource Endpoint")
public class BuildingResource {

    final protected Logger logger = LoggerFactory.getLogger(BuildingResource.class);

    @SuppressWarnings("serial")
    public static class MissingKeyException extends Exception{}
    final OperatorAppConfig conf;

    CoapResourceClient coapResourceClient = new CoapResourceClient();

    public BuildingResource(OperatorAppConfig conf) throws InterruptedException {
        this.conf = conf;
    }

    @RolesAllowed("USER")
    @GET
    @Path("/")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Floors of the building")
    public Response GetFloors(@Context ContainerRequestContext requestContext){
        try{

            logger.info("Loading all the building's Floors");
            List<String> floorList = LookupAndObserveProcess.getFloors();

            if (floorList == null)
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Floors not found")).build();

            return Response.ok(floorList).build();

        }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }

    @RolesAllowed("USER")
    @POST
    @Path("/")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value="Create a new Floor")
    public Response createFloor(@Context ContainerRequestContext req,
                                   @Context UriInfo uriInfo,
                                   String floorId) {

        try {
            logger.info("Incoming Floor Creation Request: {}", floorId);

            //Check the request
            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request payload")).build();

            CoapResponse response = coapResourceClient.postRequest("",floorId);

            return Response.created(new URI(String.format("%s/%s",uriInfo.getAbsolutePath(),response))).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get a Floor's infos")
    public Response getFloor(@Context ContainerRequestContext requestContext,
                                @PathParam("floor_id") String floorId) {

        try {

            //Check the request
            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor id Provided !")).build();

            logger.info("Loading infos for floor: {}", floorId);

            CoapResponse response = coapResourceClient.getRequest(floorId);

            return Response.ok(response).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @DELETE
    @Path("/{floor_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Delete a Floor")
    public Response deleteFloor(@Context ContainerRequestContext req,
                                 @PathParam("floorId") String floorId) {

        try {

            logger.info("Deleting Floor: {}", floorId);

            //Check the request
            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor Number Provided !")).build();

            //Check if the device is available or not

            CoapResponse response = coapResourceClient.deleteRequest(floorId);

            //Delete the location

            return Response.ok(response).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}/area")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Areas of the floor")
    public Response GetAreas(@Context ContainerRequestContext requestContext,
                                @PathParam("floor_id") String floorId){
        try{

            logger.info("Loading all the {} 's Areas", floorId);

            List<String> areaList = LookupAndObserveProcess.getAreas(floorId);

            if (areaList.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No areas found")).build();

            return Response.ok(areaList).build();

        }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }

    @RolesAllowed("USER")
    @POST
    @Path("/{floor_id}/area")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value="Create a new Area")
    public Response createArea(@Context ContainerRequestContext req,
                                @Context UriInfo uriInfo,String areaId,
                                @PathParam("floor_id") String floorId){

        try {
            logger.info("Incoming Area Creation Request: {}", areaId);

            //Check the request
            if(areaId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request payload")).build();

            CoapResponse response = coapResourceClient.postRequest(String.format("/%s", floorId), areaId);

            return Response.created(new URI(String.format("%s/%s",uriInfo.getAbsolutePath(),response))).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

//    @RolesAllowed("USER")
//    @GET
//    @Path("/{floor_id}")
//    @Timed
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value="Get a Floor's infos")
//    public Response getFloor(@Context ContainerRequestContext requestContext,
//                             @PathParam("floor_id") String floorId) {
//
//        try {
//
//            //Check the request
//            if(floorId == null)
//                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor id Provided !")).build();
//
//            logger.info("Loading infos for floor: {}", floorId);
//
//            CoapResponse response = coapResourceClient.getRequest(floorId);
//
//            return Response.ok(response).build();
//
//        } catch (Exception e){
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
//        }
//    }
//

    //TODO QUESTA DELETE HA DEI PROBLEMI. SE VIENE ELIMINATA L'AREA CONTINUO A RICEVERE LE NOTIFY DA TUTTI I DEVICE.
    //TODO DA RISOLVERE

    @RolesAllowed("USER")
    @DELETE
    @Path("/{floor_id}/area/{area_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Delete an Area and all her devices")
    public Response deleteArea(@Context ContainerRequestContext req,
                                @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId) {

        try {

            logger.info("Deleting Area: {}", areaId);

            //Check the request
            if(areaId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid AreaId Provided !")).build();

            //Check if the area exists is available or not
            if(LookupAndObserveProcess.getAreas(floorId).isEmpty()){
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No areas found")).build();

            }

            CoapResponse response = coapResourceClient.deleteRequest(String.format("%s/%s", floorId, areaId));

            LookupAndObserveProcess.deleteRelation(String.format("%s/%s", floorId, areaId));

            //Delete the location

            return Response.ok(response).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}/area/{area_id}/device")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Devices of the Area")
    public Response GetDevices(@Context ContainerRequestContext requestContext,
                             @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId){
        try{

            logger.info("Loading all the {} 's Devices in floor {}", areaId, floorId);

            List<String> devList = LookupAndObserveProcess.getDevices(String.format("%s/%s", floorId, areaId));

            if (devList.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No devices found")).build();

            return Response.ok(devList).build();

        }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }


    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}/area/{area_id}/device/{device_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get a Device's infos")
    public Response getDevice(@Context ContainerRequestContext requestContext,
                             @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId, @PathParam("device_id") String deviceId) {

        try {

            //Check the request
            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor id Provided !")).build();

            logger.info("Loading infos for device: {}, in Area {}, in floor {}", deviceId, areaId, floorId);

            CoapResponse response = coapResourceClient.getRequest(String.format("%s/%s/%s", floorId, areaId, deviceId));
            logger.info("Response Pretty Print:\n{}", Utils.prettyPrint(response));
            String text = response.getResponseText();
            logger.info("Payload: {}", text);

            if(text == null){
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Device not found")).build();
            }

            return Response.ok(text).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

//
//    @RolesAllowed("USER")
//    @DELETE
//    @Path("/{floor_id}/area/{area_id}")
//    @Timed
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value="Delete an Area and all her devices")
//    public Response deleteArea(@Context ContainerRequestContext req,
//                               @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId) {
//
//        try {
//
//            logger.info("Deleting Area: {}", areaId);
//
//            //Check the request
//            if(areaId == null)
//                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid AreaId Provided !")).build();
//
//            //Check if the area exists is available or not
//            if(LookupAndObserveProcess.getAreas(floorId).isEmpty()){
//                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No areas found")).build();
//
//            }
//
//            CoapResponse response = coapResourceClient.deleteRequest(String.format("%s/%s", floorId, areaId));
//
//            lookupAndObserveProcess.deleteRelation(String.format("%s/%s", floorId, areaId));
//
//            //Delete the location
//
//            return Response.ok(response).build();
//
//        } catch (Exception e){
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
//        }
//    }

}
