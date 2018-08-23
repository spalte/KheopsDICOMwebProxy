package online.kheops.proxy;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class BulkDataPart extends Part {
    private final InputStream inputStream;
    private final String contentLocation;

    protected BulkDataPart(InputStream inputStream, MediaType mediaType, String contentLocation) {
        super(mediaType);

        this.inputStream = inputStream;
        this.contentLocation = contentLocation;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
