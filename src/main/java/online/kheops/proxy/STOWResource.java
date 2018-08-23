package online.kheops.proxy;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/studies")
public class STOWResource {

    @HeaderParam("Content-Type")
    private MediaType contentType;

    @PUT
    @Path("/studies")
    @Consumes("multipart/related")
    public void stow(@Suspended AsyncResponse ar, InputStream in) {
        MultipartParser multipartParser = new MultipartParser(boundary());
        try {
            multipartParser.parse(in, (int pn, MultipartInputStream multipartInputStream) -> digestDicomFile(multipartInputStream));
        } catch (IOException e) {
            throw new WebApplicationException("ioexception", e);
        }


    }

    private void digestDicomFile(MultipartInputStream inputStream)
            throws IOException {

        Map<String, List<String>> headerParams = inputStream.readHeaderParams();
        DicomInputStream dicomInputStream = new DicomInputStream(inputStream);

        Attributes attributes = dicomInputStream.readDataset(-1, -1);




    }

    private String boundary() {
        String boundary = contentType.getParameters().get("boundary");
        if (boundary == null)
            throw new WebApplicationException("Missing Boundary Parameter", Response.Status.BAD_REQUEST);

        return boundary;
    }
}