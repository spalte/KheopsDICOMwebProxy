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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class STOWResource {
    private static final Logger LOG = Logger.getLogger(STOWResource.class.getName());

    @Context
    ServletContext context;

    @HeaderParam("Content-Type")
    MediaType contentType;

    @POST
    @Path("/studies")
    @Consumes("multipart/related")
    public Attributes stow(InputStream inputStream, @HeaderParam("Authorization") String AuthorizationHeader) {
        final String token;
        if (AuthorizationHeader != null) {

            if (AuthorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = AuthorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication doesn't have a username and password");
                    throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
                }

                token = split[1];
            } else if (AuthorizationHeader.toUpperCase().startsWith("BEARER ")) {
                token = AuthorizationHeader.substring(7);
            } else {
                LOG.log(Level.WARNING, "Unknown authorization header");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }

            if (token.length() == 0) {
                LOG.log(Level.WARNING, "Empty authorization token");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } else {
            LOG.log(Level.WARNING, "Missing authorization header");
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        return store(inputStream, token);
    }

    @POST
    @Path("/capability/{capability}/studies")
    @Consumes("multipart/related")
    public Attributes stowWithCapability(InputStream inputStream, @PathParam("capability") String capabilityToken) {
        return store(inputStream, capabilityToken);
    }

    private Attributes store(InputStream inputStream, String bearerToken) {
        final URI STOWServiceURI = getParameterURI("online.kheops.pacs.uri");
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");

        try (StowRS stowRS = new StowRS(STOWServiceURI.toString(), getStowContentType())) {
            STOWService stowService = new STOWService(stowRS);
            return new STOWProxy(contentType, inputStream, stowService, new AuthorizationManager(authorizationURI, bearerToken)).getResponse();
        } catch (STOWGatewayException e) {
            LOG.log(Level.SEVERE, "Gateway Error", e);
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
        } catch (STOWRequestException e) {
            LOG.log(Level.WARNING, "Bad request Error", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in the proxy", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private StowRS.ContentType getStowContentType() {
        try {
            return StowRS.ContentType.from(contentType.getParameters().get("type"));
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