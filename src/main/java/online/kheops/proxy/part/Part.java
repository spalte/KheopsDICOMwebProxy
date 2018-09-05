package online.kheops.proxy.part;

import online.kheops.proxy.ContentLocation;
import online.kheops.proxy.SeriesID;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.ws.rs.MediaTypes;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.*;


public abstract class Part implements AutoCloseable {
    private final MediaType mediaType;

    Part(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public static Part getInstance(MultipartInputStream multipartInputStream) throws IOException {
        Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
        ContentLocation contentLocation = ContentLocation.valueOf(getHeaderParamValue(headerParams, "content-location"));
        MediaType mediaType = MediaType.valueOf(getHeaderParamValue(headerParams, "content-type"));

        if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_TYPE)) {
            return new DICOMPart(multipartInputStream, mediaType);
        } else if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_JSON_TYPE) || MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_JSON_TYPE)) {
            return new DICOMMetadataPart(multipartInputStream, mediaType);
        } else {
            return new BulkDataPart(multipartInputStream, mediaType, contentLocation);
        }
    }

    public Set<ContentLocation> getBulkDataLocations() {
        return Collections.emptySet();
    }

    public Optional<ContentLocation> getContentLocation() {
        return Optional.empty();
    }

    public Optional<SeriesID> getSeriesID() {
        return Optional.empty();
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
