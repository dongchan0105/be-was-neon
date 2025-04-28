package frontHandler.adapter;

import dto.HttpRequest;
import frontHandler.ModelView;
import handler.ReturnViewPathHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class ReturnViewPathAdapter implements HandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ReturnViewPathAdapter.class);

    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ReturnViewPathHandler);
    }

    @Override
    public ModelView handle(HttpRequest request, Object handler) {
        ReturnViewPathHandler controller = (ReturnViewPathHandler) handler;

        // 1. 파라미터 맵 복사
        Map<String, String> paramMap = new HashMap<>(request.params());

        // 세션 쿠키 추가
        paramMap.put(SESSION_COOKIE_NAME,
                request.cookies().getOrDefault(SESSION_COOKIE_NAME, "no-cookie"));
        log.info("check cookieValue = {}", paramMap.get(SESSION_COOKIE_NAME));

        //-multipart data들 paramMap에 담기
        paramMap.put("fileItems", request.fileItems().toString());

        // 2. 모델 객체 생성
        Map<String, Object> model = new HashMap<>();

        // ── 여기서 multipart로 넘어온 파일 아이템도 모델에 담아줍니다.
        //model.put("fileItems", request.fileItems());

        // 3. 핸들러 실행 ➔ 뷰 이름 반환
        String viewName = controller.process(paramMap, model);
        log.info("model = {}, viewName = {}", model, viewName);

        ModelView mv = new ModelView(viewName);
        mv.setModel(model);
        return mv;
    }

}

