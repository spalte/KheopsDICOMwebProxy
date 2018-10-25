package online.kheops.proxy.multipart;

import javax.ws.rs.core.MediaType;

public class StreamingMultiPart {
    private MultipartStreamingOutput multipartStreamingOutput;

    public StreamingMultiPart(MediaType mediaType, MultipartStreamingOutput multipartStreamingOutput) {
        this.multipartStreamingOutput = multipartStreamingOutput;
    }

    public MultipartStreamingOutput getMultipartStreamingOutput() {
        return multipartStreamingOutput;
    }

    public void setMultipartStreamingOutput(MultipartStreamingOutput multipartStreamingOutput) {
        this.multipartStreamingOutput = multipartStreamingOutput;
    }
}
