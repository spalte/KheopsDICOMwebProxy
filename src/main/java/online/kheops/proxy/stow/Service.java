package online.kheops.proxy.stow;


import online.kheops.proxy.multipart.MultipartOutputStream;
import online.kheops.proxy.multipart.StreamingBodyPart;
import online.kheops.proxy.part.BulkDataPart;
import online.kheops.proxy.part.DICOMMetadataPart;
import online.kheops.proxy.part.DICOMPart;
import online.kheops.proxy.part.Part;
import org.dcm4che3.ws.rs.MediaTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public final class Service {

    private final MultipartOutputStream multipartOutputStream;

    Service(MultipartOutputStream multipartOutputStream) {
        this.multipartOutputStream = multipartOutputStream;

    }

    public void write(Part part) throws GatewayException {
        if (part instanceof DICOMPart) {
            writeDICOM((DICOMPart) part);
        } else if (part instanceof DICOMMetadataPart) {
            writeMetadata((DICOMMetadataPart) part);
        } else if (part instanceof BulkDataPart) {
            writeBulkData((BulkDataPart) part);
        } else {
            throw new ClassCastException("Unable to cast the part to a known Part class");
        }
    }


    public void writeBulkData(BulkDataPart bulkDataPart) {

    }
    public void writeMetadata(DICOMMetadataPart bulkDataPart) {

    }

    public void writeDICOM(DICOMPart dicomPart) throws GatewayException {
        try {
            final InputStream inputStream = Files.newInputStream(dicomPart.getCacheFilePath());
            multipartOutputStream.writePart(new StreamingBodyPart(inputStream, MediaTypes.APPLICATION_DICOM_TYPE));
            inputStream.close();
        } catch (IOException e) {
            throw new GatewayException("Failed to store DICOMPart", e);
        }
    }
}
