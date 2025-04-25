package dto;

import org.apache.commons.fileupload.FileItem;
import java.util.Map;

/**
 * HTTP 요청 정보 (multipart/form-data 지원)
 */
public record HttpRequest(
        String method,
        String path,
        Map<String, String> headers,
        Map<String, String> cookies,
        Map<String, String> params,               // GET/URL-encoded 폼 파라미터
        Map<String, FileItem> fileItems           // multipart로 넘어온 파일들
) {}
