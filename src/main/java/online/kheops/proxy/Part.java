package online.kheops.proxy;

import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.ws.rs.MediaTypes;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.List;
import java.util.Map;


// a Part can be streamed out using the STOWService
public abstract class Part implements AutoCloseable {
    private final MediaType mediaType;

    protected Part(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public static Part getInstance(MultipartInputStream multipartInputStream) throws IOException {
        Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
        String contentLocation = getHeaderParamValue(headerParams, "content-location");
        String contentType = getHeaderParamValue(headerParams, "content-type");
        MediaType mediaType = MediaType.valueOf(contentType);

        if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_TYPE)) {
            return new DICOMPart(multipartInputStream, mediaType);
        } else if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_JSON_TYPE) || MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_JSON_TYPE)) {
            return new DICOMMetadataPart(multipartInputStream, mediaType);
        } else {
            return new BulkDataPart(multipartInputStream, mediaType, contentLocation);
        }
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    private static String getHeaderParamValue(Map<String, List<String>> headerParams, String key) {
        List<String> list = headerParams.get(key);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public void close() throws IOException {}
}
