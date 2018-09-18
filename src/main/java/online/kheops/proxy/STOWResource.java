package online.kheops.proxy;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.web.StowRS;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class STOWResource {
    private static final Logger LOG = Logger.getLogger(STOWResource.class.getName());

    @Context
    ServletContext context;

    @HeaderParam("Content-Type")
    MediaType contentType;

    @GET
    @Path("/stupid")
    public String stupid() {
        return "stupid";
    }

    @POST
    @Path("/studies")
//    @Consumes("multipart/related")
    public Attributes stow(InputStream inputStream) {
        final URI STOWServiceURI = getParameterURI("online.kheops.pacs.uri");
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");

        try (StowRS stowRS = new StowRS(STOWServiceURI.toString(), getStowContentType())) {
            STOWService stowService = new STOWService(stowRS);
            return new STOWProxy(contentType, inputStream, stowService, new AuthorizationManager(authorizationURI)).getResponse();
        } catch (STOWGatewayException e) {
            LOG.log(Level.SEVERE, "Gateway Error", e);
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
        } catch (STOWRequestException e) {
            LOG.log(Level.WARNING, "Bad request Error", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in the proxy", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private StowRS.ContentType getStowContentType() {
        try {
            return StowRS.ContentType.from(contentType.getType());
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Bad request Error", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private URI getParameterURI(String parameter) {
        try {
            return new URI(context.getInitParameter(parameter));
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "Error with the STOWServiceURI", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}