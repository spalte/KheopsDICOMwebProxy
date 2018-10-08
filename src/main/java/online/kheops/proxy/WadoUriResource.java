package online.kheops.proxy;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class WadoUriResource {
    private static final Logger LOG = Logger.getLogger(WadoUriResource.class.getName());

    private static final Client CLIENT = ClientBuilder.newClient();

    @Context
    private UriInfo uriInfo;

    @Context
    private ServletContext context;

    @HeaderParam("Accept")
    private String acceptParam;

    @HeaderParam("Accept-Charset")
    private String acceptCharsetParam;

    @GET
    @Path("/password/dicomweb/wado")
    public Response wado(@HeaderParam("Authorization") String authorizationHeader) {
        return webAccess(authorizationHeaderToToken(authorizationHeader));
    }

    @GET
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/wado")
    public Response wadoWithCapability(@PathParam("capability") String capabilityToken) {
        return webAccess(capabilityToken);
    }

    private Response webAccess(String token) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");

        WebTarget webTarget = CLIENT.target(wadoServiceURI).path("wado");

        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        final List<String> studyInstanceUIDs = queryParameters.get("studyUID");
        if (studyInstanceUIDs == null || studyInstanceUIDs.size() != 1) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        final List<String> seriesInstanceUIDs = queryParameters.get("seriesUID");
        if (seriesInstanceUIDs == null || seriesInstanceUIDs.size() != 1) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        final String studyInstanceUID = studyInstanceUIDs.get(0);
        final String seriesInstanceUID = seriesInstanceUIDs.get(0);

        final AccessToken accessToken;
        try {
            accessToken = AccessToken.createBuilder(authorizationURI)
                    .withCapability(token)
                    .withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID))
                    .build();
        } catch (AccessTokenException e) {
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
        }

        for (String parameter: queryParameters.keySet()) {
            webTarget = webTarget.queryParam(parameter, queryParameters.get(parameter).toArray());
        }

        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header("Authorization", "Bearer " + accessToken.getToken());
        if (acceptParam != null) {
            invocationBuilder.accept(acceptParam);
        } else {
            invocationBuilder.accept("application/dicom");
        }

        if (acceptCharsetParam != null) {
            invocationBuilder.header("Accept-Charset", acceptCharsetParam);
        }

        Response wadoResponse =  invocationBuilder.get(Response.class);

        return wadoResponse;
    }


    private String authorizationHeaderToToken(String authorizationHeader) {
        final String token;
        if (authorizationHeader != null) {

            if (authorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = authorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication doesn't have a username and password");
                    throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
                }

                token = split[1];
            } else if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
                token = authorizationHeader.substring(7);
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

        return token;
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


