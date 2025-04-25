package handler;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class IndexHandler implements ReturnViewPathHandler{

    private static final Logger log = LoggerFactory.getLogger(IndexHandler.class);

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {

        String sessionId = paramMap.get(SESSION_COOKIE_NAME);
        log.info("sessionId?!?! = {}", sessionId);
        User user = SessionManager.getUser(sessionId);
        log.info("user is in ?? = {}", user);

        // 모델에 사용자 정보 추가
        model.put("user", user);
        return "dynamic/index";
    }
}
