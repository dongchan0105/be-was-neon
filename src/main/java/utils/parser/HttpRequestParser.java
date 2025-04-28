package utils.parser;

import dto.HttpRequest;
import exception.ClientException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
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
        Map<String, String> headers = parseHeaders(reader);

        // 3. Cookie 파싱
        Map<String, String> cookies = parseCookies(headers);

        // 4. Body 파싱
        Map<String, String> paramMap = new HashMap<>();
        Map<String, FileItem> fileItems = new HashMap<>();

        if (ServletFileUpload.isMultipartContent(new SimpleRequestContext(headers, input))) {
            try {
                parseMultipart(input, headers, paramMap, fileItems);
            } catch (Exception e) {
                log.error("Multipart parsing error", e);
            }
        } else {
            parseUrlEncodedForm(method, fullPath, reader, headers, paramMap);
        }

        // 5. HttpRequest 객체 생성
        return new HttpRequest(
                method,
                URLDecoder.decode(stripQueryString(fullPath), StandardCharsets.UTF_8),
                headers,
                cookies,
                paramMap,
                fileItems
        );
    }

    private static Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
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
        return headers;
    }

    private static Map<String, String> parseCookies(Map<String, String> headers) {
        Map<String, String> cookies = new HashMap<>();
        if (headers.containsKey("cookie")) {
            String[] cookiePairs = headers.get("cookie").split(";");
            for (String pair : cookiePairs) {
                String[] keyValue = pair.trim().split("=", 2);
                if (keyValue.length == 2) {
                    cookies.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return cookies;
    }

    private static void parseMultipart(InputStream input, Map<String, String> headers, Map<String, String> paramMap, Map<String, FileItem> fileItems) throws Exception {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        FileItemIterator iter = upload.getItemIterator(new SimpleRequestContext(headers, input));

        while (iter.hasNext()) {
            FileItemStream item = iter.next();
            try (InputStream stream = item.openStream()) {
                if (item.isFormField()) {
                    paramMap.put(item.getFieldName(), readStreamAsString(stream));
                } else {
                    fileItems.put(item.getFieldName(), createFileItem(item, stream));
                }
            }
        }
    }

    private static String readStreamAsString(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines().reduce((a, b) -> a + "\n" + b).orElse("");
        }
    }

    private static FileItem createFileItem(FileItemStream item, InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        FileItem fileItem = factory.createItem(item.getFieldName(), item.getContentType(), false, item.getName());
        fileItem.getOutputStream().write(baos.toByteArray());
        return fileItem;
    }

    private static void parseUrlEncodedForm(String method, String fullPath, BufferedReader reader, Map<String, String> headers, Map<String, String> paramMap) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            int idx = fullPath.indexOf('?');
            if (idx >= 0) {
                parseKeyValuePairs(fullPath.substring(idx + 1), paramMap);
            }
        }
        if ("POST".equalsIgnoreCase(method) && headers.containsKey("content-length")) {
            int length = Integer.parseInt(headers.get("content-length"));
            char[] bodyChars = new char[length];
            reader.read(bodyChars, 0, length);
            String body = new String(bodyChars);
            parseKeyValuePairs(body, paramMap);
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

    private static String stripQueryString(String fullPath) {
        int idx = fullPath.indexOf('?');
        return idx >= 0 ? fullPath.substring(0, idx) : fullPath;
    }
}