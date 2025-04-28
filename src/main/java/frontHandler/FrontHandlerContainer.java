package frontHandler;

import dto.HttpResponse;
import dto.RouteKey;
import frontHandler.adapter.HandlerAdapter;
import frontHandler.adapter.ReturnViewPathAdapter;
import handler.*;
import handler.user.UserListHandler;
import handler.user.UserRequestHandler;
import utils.parser.HttpResponseParser;
import response.HttpResponseRender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dto.HttpRequest;
import utils.parser.HttpRequestParser;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontHandlerContainer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FrontHandlerContainer.class);
    private final Socket connection;
    private final StaticRequestHandler staticRequestHandler;

    private final Map<RouteKey, Object> handlerMappingMap = new HashMap<>();
    private final List<HandlerAdapter> handlerAdapters = new ArrayList<>();

    public FrontHandlerContainer(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.staticRequestHandler = new StaticRequestHandler();

        // 핸들러 매핑 초기화
        initHandlerMappingMap();
        initHandlerAdapters();
    }


    private void initHandlerMappingMap() {
        handlerMappingMap.put(new RouteKey("/create", "POST"), new UserRequestHandler());
        handlerMappingMap.put(new RouteKey("/login", "POST"), new LoginHandler());
        handlerMappingMap.put(new RouteKey("/user/list", "GET"), new UserListHandler());
        handlerMappingMap.put(new RouteKey("/", "GET"), new IndexHandler());
        handlerMappingMap.put(new RouteKey("/index", "GET"), new IndexHandler());
        handlerMappingMap.put(new RouteKey("/write","POST"),new ArticleWriteHandler());
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ReturnViewPathAdapter());
    }

    @Override
    public void run() {
        logger.debug("New Client Connect! Connected IP: {}, Port: {}",
                connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {

            // HTTP 요청 파싱
            HttpRequest request = HttpRequestParser.parse(in);

            // 라우팅 처리
            handleRouting(request, out);

        } catch (IOException e) {
            logger.info("IOException occur");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRouting(HttpRequest request,OutputStream out) throws IOException {

        // 1. 핸들러 매핑 조회
        Object handler = getHandler(request.path(), request.method());

        // 2. 어댑터 조회
        HandlerAdapter adapter = getHandlerAdapter(handler);

        logger.info("handler: {}, adapter: {}", handler, adapter);

        if (adapter != null) {
            logger.warn("using handler adapter {}", handler.getClass().getName());
            // 3. 어댑터를 통한 핸들러 실행
            ModelView mv = adapter.handle(request, handler);

            // 4. 뷰 렌더링
            renderView(mv, out);
        } else {
            // 기본 정적 리소스 처리
            staticRequestHandler.handleStaticRequest(request.path(), out);
        }
    }

    private Object getHandler(String path, String method) {
        return handlerMappingMap.get(new RouteKey(path, method));
    }

    private HandlerAdapter getHandlerAdapter(Object handler) {
        return handlerAdapters.stream()
                .filter(adapter -> adapter.supports(handler))
                .findFirst()
                .orElse(null);
    }

    private void renderView(ModelView mv, OutputStream out) throws IOException {
        HttpResponse response = HttpResponseParser.makeHttpResponse(mv);
        HttpResponseRender.render(out,response);
    }

}
