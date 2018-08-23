package online.kheops.proxy;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.ws.rs.MediaTypes;
import org.xml.sax.SAXException;

import javax.json.Json;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class DICOMMetadataPart extends Part {
    private final Attributes dataset;
    private final Set<String> bulkDataLocations;


    protected DICOMMetadataPart(InputStream inputStream, MediaType mediaType) throws IOException {
        super(mediaType);

        if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_XML_TYPE)) {
            try {
                dataset = SAXReader.parse(inputStream);
            } catch (ParserConfigurationException | SAXException e) {
                throw new IOException("Unable to read DICOM XML", e);
            }
        } else if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_XML_TYPE)) {
            try {
                JSONReader reader = new JSONReader(Json.createParser(new InputStreamReader(inputStream, "UTF-8")));
                dataset = reader.readDataset(null);
            } catch (JsonParsingException e) {
                throw new IOException("Unable to parse the DICOM JSON", e);
            }
        } else {
            throw new IllegalArgumentException("Invalid Media Type");
        }

        try {
            bulkDataLocations = parseBulkDataLocations();
        } catch (Exception e) {
            throw new IOException("Error while parsing for Bulk Data", e);
        }
    }

    protected DICOMMetadataPart(Attributes dataset, MediaType mediaType) throws IOException {
        super(mediaType);
        this.dataset = dataset;
        this.bulkDataLocations = Collections.emptySet();
    }

    public SeriesID getSeriesID() {
        return SeriesID.from(dataset);
    }

    public String getTransferSyntax() {
        return MediaTypes.getTransferSyntax(getMediaType());
    }

    private Set<String> parseBulkDataLocations() throws Exception {
        Set<String> bulkDataLocations = new HashSet<>();

        dataset.accept((attrs1, tag, vr, value) -> {
            if (value instanceof BulkData) {
                bulkDataLocations.add(((BulkData) value).getURI());
            }
            return false;
        }, true);

        return bulkDataLocations;
    }

    public Set<String> getBulkDataLocations() {
        return bulkDataLocations;
    }

    public Attributes getDataset() {
        return dataset;
    }
}
