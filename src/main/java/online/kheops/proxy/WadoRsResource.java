package online.kheops.proxy;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/")
public class WadoRsResource {
    private static final Logger LOG = Logger.getLogger(WadoRsResource.class.getName());

    private static final Client CLIENT = ClientBuilder.newClient();

    @Context
    private UriInfo uriInfo;

    @Context
    private ServletContext context;

    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response wado(@HeaderParam("Authorization") String authorizationHeader,
                         @PathParam("studyInstanceUID") String studyInstanceUID,
                         @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response wadoWithCapability(@PathParam("capability") String capabilityToken,
                                       @PathParam("studyInstanceUID") String studyInstanceUID,
                                       @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.from(capabilityToken));
    }

    private Response webAccess(String studyInstanceUID, String seriesInstanceUID, AuthorizationToken authorizationToken) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");

        final AccessToken accessToken;
        try {
            accessToken = AccessToken.createBuilder(authorizationURI)
                    .withCapability(authorizationToken.getToken())
                    .withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID))
                    .build();
        } catch (AccessTokenException e) {
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
        }

        Pattern p = Pattern.compile("(studies/(?:(?:[0-9]+[.])*[0-9]+)/series.*)");
        Matcher m = p.matcher(uriInfo.getPath());

        final String resource;
        if (m.find()) {
            resource = m.group(1);
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        WebTarget webTarget = CLIENT.target(wadoServiceURI).path(resource);

        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header("Authorization", "Bearer " + accessToken.getToken());
        // TODO don't forget about headers

        return invocationBuilder.get(Response.class);
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
