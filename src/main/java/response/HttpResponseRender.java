package response;

import dto.HttpResponse;
import exception.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseRender {

    private static final Logger log = LoggerFactory.getLogger(HttpResponseRender.class);

    public static void render(OutputStream out, HttpResponse response) {
        try {
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeBytes("HTTP/1.1 " + response.getStatusCode() + " " + response.getStatusText() + "\r\n");
            if (response.getContentType() != null) {
                dos.writeBytes("Content-Type: " + response.getContentType() + "\r\n");
            }
            dos.writeBytes("Content-Length: " + (response.getBody() != null ? response.getBody().length : 0) + "\r\n");
            for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
                dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
            }
            for (String cookieName : response.getCookies().keySet()) {
                String cookieValue = response.getCookies().get(cookieName);
                String cookieStr = cookieName + "=" + cookieValue + "; Path=/; HttpOnly; Max-Age=3600"; // 속성도 추가 가능
                dos.writeBytes("Set-Cookie: " + cookieStr + "\r\n");
            }
            dos.writeBytes("Connection: close\r\n");
            dos.writeBytes("\r\n");
            if (response.getBody() != null) {
                dos.write(response.getBody());
            }
            dos.flush();
        } catch (IOException e) {
            log.info("IOException Occur");
        }
    }


    public static void sendResponse(OutputStream out, int statusCode,
                                    String statusText, String contentType, byte[] body) {
        try{
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeBytes("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("Connection: close\r\n");
            dos.writeBytes("\r\n");
            dos.write(body);
            dos.flush();
        } catch (IOException e){
            log.info("IOException Occur");
        }

    }


    public static void sendErrorResponse(OutputStream out, HttpException e) {
        String htmlBody = String.format(
                "<!DOCTYPE html><html><head><title>%d %s</title></head>" +
                        "<body><h1>%d %s</h1></body></html>",
                e.getStatusCode(), e.getMessage(),e.getStatusCode(), e.getMessage());
        byte[] body = htmlBody.getBytes(StandardCharsets.UTF_8);
        sendResponse(out, e.getStatusCode(), e.getMessage(), "text/html;charset=utf-8", body);
    }


}
