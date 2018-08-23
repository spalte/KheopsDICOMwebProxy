package online.kheops.proxy;

import org.dcm4che3.data.Attributes;
import org.glassfish.jersey.media.multipart.MultiPart;

public class SeriesID extends MultiPart {
    private final String studyUID;
    private final String seriesUID;

    public SeriesID(String studyUID, String seriesUID) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
    }

    public static SeriesID from(Attributes attributes) {

    }

    public String getStudyUID() {
        return studyUID;
    }

    public String getSeriesUID() {
        return seriesUID;
    }
}
