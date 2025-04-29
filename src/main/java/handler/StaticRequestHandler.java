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
        if (path.endsWith("/")) {
            path += "index.html";
        }

        path = URLDecoder.decode(path, StandardCharsets.UTF_8);

        // 수정 포인트: File 대신 InputStream으로 읽기
        InputStream inputStream = StaticRequestHandler.class.getClassLoader().getResourceAsStream("static" + path);

        if (inputStream == null) {
            logger.warn("Resource not found: static{}", path);
            HttpResponseRender.sendErrorResponse(out, new ClientException(NOT_FOUND));
            return;
        }

        try {
            byte[] body = readInputStreamToByteArray(inputStream);
            String contentType = determineContentType(path);
            logger.info("Served resource: {}", path);
            HttpResponseRender.sendResponse(out, 200, "OK", contentType, body);
        } catch (IOException e) {
            logger.error("Failed to serve static resource: {}", path, e);
            HttpResponseRender.sendErrorResponse(out, new ClientException(FORBIDDEN));
        }
    }

    private byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        return bos.toByteArray();
    }

    public static String determineContentType(String path) {
        String fileName = path.toLowerCase();
        String fileExtension = getFileExtension(fileName);

        return ContentType.getMimeTypeByExtension(fileExtension);
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
