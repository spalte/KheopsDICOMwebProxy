package online.kheops.proxy;


import org.dcm4che3.data.Attributes;

import javax.ws.rs.core.MediaType;
import java.net.URI;

// used to send data to the PACS
public final class STOWService implements AutoCloseable {

    STOWService(URI stowUri, MediaType mediaType) {

    }

    public void write(Part part) {

    }

    public void write(Attributes attributes, String tsuid) {

    }

    @Override
    public void close() throws Exception {

    }
}
