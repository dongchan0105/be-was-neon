package handler;

import session.SessionManager;

import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class LogoutHandler implements ReturnViewPathHandler<Map<String, String>>{

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {

        String sessionId = paramMap.get(SESSION_COOKIE_NAME);
        SessionManager.expireSession(sessionId);

        return "redirect:/index";
    }
}
