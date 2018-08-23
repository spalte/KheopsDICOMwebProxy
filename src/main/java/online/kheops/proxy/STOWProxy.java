package online.kheops.proxy;

import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.ws.rs.MediaTypes;

import java.io.IOException;
import java.util.jar.Attributes;

public final class STOWProxy implements AutoCloseable {

    private final AuthorizationManager authorizationManager = new AuthorizationManager();
    private final STOWService stowService;

    public void writePart(Part part) throws IOException  {
        if (MediaTypes.equalsIgnoreParameters(part.getMediaType(), MediaTypes.APPLICATION_DICOM_TYPE)) {
            writeDicomPart(part);
        }

    }

    public Attributes getResponse() {

    }



    private void writeDicomPart(Part part) throws IOException {
        DicomInputStream dicomInputStream = new DicomInputStream(part.getInputStream());
        org.dcm4che3.data.Attributes attributes = dicomInputStream.readDataset(-1, -1);

        getAuthorization(part.getS);

        stowService.write(attributes, part.getMediaType().getParameters().get("transfer-syntax"));
    }


    private void getAuthorization(SeriesID seriesID) throws IOException{
        try {
            authorizationManager.getAuthorization(seriesID);
        } catch (AuthorizationManagerException e) {
            throw new IOException("Unable to get authorization", e);
        }
    }

}
