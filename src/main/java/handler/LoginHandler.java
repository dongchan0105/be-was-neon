package handler;

import dto.LoginRequest;
import exception.ClientException;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.LoginService;
import session.SessionManager;

import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class LoginHandler implements ReturnViewPathHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        // 1. 파라미터 추출
        String userId = paramMap.getOrDefault("userId", "");
        String password = paramMap.getOrDefault("password", "");

        try {
            // 2. 로그인 시도
            User loginUser = LoginService.login(new LoginRequest(userId, password));
            model.put("loginUser", loginUser);

            // 3. 세션 생성 및 모델에 저장
            String sessionId = SessionManager.createSession(loginUser);
            model.put(SESSION_COOKIE_NAME, sessionId);

            // 4. 성공 시 리다이렉트
            return "redirect:/index";

        } catch (ClientException e) {
            logger.warn("Login failed: {}", e.getMessage());
            return "redirect:/user/login_failed.html";
        }
    }



}
