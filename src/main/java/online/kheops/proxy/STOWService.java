package online.kheops.proxy;


import online.kheops.proxy.part.BulkDataPart;
import online.kheops.proxy.part.DICOMMetadataPart;
import online.kheops.proxy.part.DICOMPart;
import online.kheops.proxy.part.Part;
import org.dcm4che3.data.Attributes;
import org.weasis.dicom.web.StowRS;

import java.io.IOException;

public final class STOWService {
    private final StowRS stowRS;

    STOWService(StowRS stowRS) {
        this.stowRS = stowRS;
    }

    public void write(Part part) throws STOWGatewayException {
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

    public void writeDICOM(DICOMPart bulkDataPart) {

    }


    public Attributes getResponse() throws IOException {

    }

}
