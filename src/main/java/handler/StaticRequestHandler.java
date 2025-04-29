package handler;

import exception.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import domain.ContentType;
import response.HttpResponseRender;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static domain.error.HttpClientError.*;

public class StaticRequestHandler implements ReturnViewPathHandler<Map<String, String>> {

    private static final Logger logger = LoggerFactory.getLogger(StaticRequestHandler.class);

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        logger.info("Processing StaticRequestHandler");
        return "HELLO WORLD";
    }

    public void handleStaticRequest(String path, OutputStream out) {
        try {
            if (path.endsWith("/")) {
                path += "index.html";
            }

            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
            String resourcePath = "static" + path; // classpath 기준

            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (resourceStream == null) {
                logger.warn("Static resource not found: {}", resourcePath);
                HttpResponseRender.sendErrorResponse(out, new ClientException(NOT_FOUND));
                return;
            }

            byte[] body = resourceStream.readAllBytes();
            String contentType = determineContentType(resourcePath);

            logger.info("Serving static file: {}", resourcePath);
            HttpResponseRender.sendResponse(out, 200, "OK", contentType, body);
        } catch (Exception e) {
            logger.error("Error handling static request", e);
            HttpResponseRender.sendErrorResponse(out, new ClientException(FORBIDDEN));
        }
    }

    public static String determineContentType(String resourcePath) {
        String lowerPath = resourcePath.toLowerCase();
        String extension = getFileExtension(lowerPath);
        return ContentType.getMimeTypeByExtension(extension);
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
