package online.kheops.proxy.multipart;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

public interface MultipartStreamingOutput extends StreamingOutput {

    void write(MultipartOutputStream output) throws IOException;

}
