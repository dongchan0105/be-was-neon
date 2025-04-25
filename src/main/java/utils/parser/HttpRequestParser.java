package utils.parser;

import dto.HttpRequest;
import exception.ClientException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static domain.error.HttpClientError.findByStatusCode;

/**
 * HTTP 요청 파싱기 (multipart/form-data 지원)
 */
public class HttpRequestParser {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestParser.class);

    public static HttpRequest parse(InputStream input) throws IOException, ClientException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        // 1. Request Line 파싱
        String requestLine = reader.readLine();
        if (requestLine == null) {
            throw new ClientException(findByStatusCode(400));
        }
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) {
            throw new ClientException(findByStatusCode(400));
        }

        String method = requestParts[0];
        String fullPath = requestParts[1];

        // 2. Header 파싱
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                String name = line.substring(0, idx).trim().toLowerCase();
                String value = line.substring(idx + 1).trim();
                headers.put(name, value);
            }
        }

        Map<String, String> cookies = new HashMap<>();
        if (headers.containsKey("cookie")) {
            String cookieHeader = headers.get("cookie");
            parseCookies(cookieHeader, cookies);
            log.info("Cookies: {}", cookies);
        }

        // 3. Body 또는 multipart 처리
        Map<String, String> paramMap = new HashMap<>();
        Map<String, FileItem> fileItems = new HashMap<>();
        String contentType = headers.getOrDefault("Content-Type", "");

        if (ServletFileUpload.isMultipartContent(new SimpleRequestContext(headers, input))) {
            // multipart/form-data 처리
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            FileItemIterator iter;
            try {
                iter = upload.getItemIterator(new SimpleRequestContext(headers, input));
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    try (InputStream stream = item.openStream()) {
                        if (item.isFormField()) {
                            String value = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                                    .lines()
                                    .reduce((a, b) -> a + "\n" + b)
                                    .orElse("");
                            paramMap.put(item.getFieldName(), value);
                        } else {
                            // 파일 필드
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = stream.read(buffer)) != -1) {
                                baos.write(buffer, 0, len);
                            }
                            // Commons FileUpload FileItem으로 보관
                            DiskFileItemFactory tmpFactory = new DiskFileItemFactory();
                            FileItem fileItem = tmpFactory.createItem(item.getFieldName(),
                                    item.getContentType(), false, item.getName());
                            fileItem.getOutputStream().write(baos.toByteArray());
                            fileItems.put(item.getFieldName(), fileItem);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Multipart parsing error", e);
            }
        } else {
            // URL 인코딩된 파라미터 처리 (GET query 또는 POST body)
            String queryString = null;
            if ("GET".equalsIgnoreCase(method)) {
                int idx = fullPath.indexOf('?');
                if (idx >= 0) {
                    queryString = fullPath.substring(idx + 1);
                    fullPath = fullPath.substring(0, idx);
                }
            }
            // GET 쿼리 파싱
            if (queryString != null) {
                parseKeyValuePairs(queryString, paramMap);
            }
            // POST body 파싱
            if ("POST".equalsIgnoreCase(method) && headers.containsKey("Content-Length")) {
                int length = Integer.parseInt(headers.get("Content-Length"));
                char[] bodyChars = new char[length];
                reader.read(bodyChars, 0, length);
                String body = new String(bodyChars);
                parseKeyValuePairs(body, paramMap);
            }
        }

        // 4. HttpRequest 객체에 담기
        return new HttpRequest(method,URLDecoder.decode(fullPath, StandardCharsets.UTF_8),headers,cookies,paramMap,
                fileItems);

    }

    private static void parseCookies(String cookieHeader, Map<String, String> cookies) {
        String[] cookiePairs = cookieHeader.split(";");
        for (String pair : cookiePairs) {
            String[] keyValue = pair.trim().split("=", 2);
            if (keyValue.length == 2) {
                cookies.put(keyValue[0], keyValue[1]);
            }
        }
    }

    private static void parseKeyValuePairs(String source, Map<String, String> paramMap) {
        for (String pair : source.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    String key = URLDecoder.decode(kv[0], "UTF-8");
                    String value = URLDecoder.decode(kv[1], "UTF-8");
                    paramMap.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Commons FileUpload를 위한 간단한 RequestContext 구현
     */
    private static class SimpleRequestContext implements RequestContext {
        private final Map<String, String> headers;
        private final InputStream input;

        SimpleRequestContext(Map<String, String> headers, InputStream input) {
            this.headers = headers;
            this.input = input;
        }

        @Override
        public String getCharacterEncoding() {
            return headers.getOrDefault("Content-Encoding", StandardCharsets.UTF_8.name());
        }

        @Override
        public String getContentType() {
            return headers.get("Content-Type");
        }

        @Override
        public int getContentLength() {
            try {
                return Integer.parseInt(headers.getOrDefault("Content-Length", "-1"));
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return input;
        }
    }
}