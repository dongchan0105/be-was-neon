package utils.parser;

import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Commons FileUpload를 위한 간단한 RequestContext 구현
 */
public class SimpleRequestContext implements RequestContext {
    private final Map<String, String> headers;
    private final InputStream input;

    public SimpleRequestContext(Map<String, String> headers, InputStream input) {
        this.headers = headers;
        this.input = input;
    }

    @Override
    public String getCharacterEncoding() {
        return headers.getOrDefault("content-encoding", "UTF-8");
    }

    @Override
    public String getContentType() {
        return headers.get("content-type");
    }

    @Override
    public int getContentLength() {
        try {
            return Integer.parseInt(headers.getOrDefault("content-length", "-1"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return input;
    }
}
