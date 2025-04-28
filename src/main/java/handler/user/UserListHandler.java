package handler.user;

import handler.ReturnViewPathHandler;
import model.User;
import service.UserService;

import java.util.List;
import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class UserListHandler implements ReturnViewPathHandler<Map<String, String>> {

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {

        String cookieId = paramMap.getOrDefault(SESSION_COOKIE_NAME,null);

        if (cookieId == null) {
            return "redirect:/login.html";
        }

        List<User> users = UserService.getAllUsers();
        model.put("users", users);
        return "user/list";
    }
}
