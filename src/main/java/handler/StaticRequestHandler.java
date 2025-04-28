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
    private static final String STATIC_DIRECTORY = "src/main/resources/static";

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
        File file = new File(STATIC_DIRECTORY + File.separator + path.replace("/", File.separator));

        try {
            validateFilePath(file);
            if (file.exists() && !file.isDirectory()) {
                serveFile(file, out);
            } else if (file.isDirectory()) {
                handleDirectoryRequest(file, out);
            } else {
                HttpResponseRender.sendErrorResponse(out,new ClientException(NOT_FOUND));
            }
        } catch (Exception e) {
            HttpResponseRender.sendErrorResponse(out,new ClientException(FORBIDDEN));
        }
    }

    private void serveFile(File file, OutputStream out) throws IOException {
        byte[] body = readFileToByteArray(file);
        String contentType = determineContentType(file);
        logger.info("Served file: {}", file.getPath());
        HttpResponseRender.sendResponse(out, 200, "OK", contentType, body); // 헬퍼 클래스 사용
    }

    private void handleDirectoryRequest(File directory, OutputStream out) throws IOException {
        File indexFile = new File(directory, "index.html");
        if (indexFile.exists()) {
            serveFile(indexFile, out);
        } else {
            HttpResponseRender.sendErrorResponse(out, new ClientException(NOT_FOUND)); // 헬퍼 클래스 사용
        }
    }

    private byte[] readFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length())) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }

    private void validateFilePath(File file) throws IOException {
        String canonicalPath = file.getCanonicalPath();
        String staticDirCanonical = new File(STATIC_DIRECTORY).getCanonicalPath();

        if (!canonicalPath.startsWith(staticDirCanonical)) {
            throw new SecurityException("Invalid file access attempt");
        }
    }

    public static String determineContentType(File file) {
        // 파일 이름에서 확장자 추출
        String fileName = file.getName().toLowerCase();
        String fileExtension = getFileExtension(fileName);

        return ContentType.getMimeTypeByExtension(fileExtension);
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            // 확장자가 없거나 잘못된 경우 빈 문자열 반환
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

}
