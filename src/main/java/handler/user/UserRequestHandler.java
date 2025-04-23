package handler.user;

import dto.UserCreateRequest;
import exception.ClientException;
import handler.ReturnViewPathHandler;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;

import java.util.Map;

import static domain.error.HttpClientError.BAD_REQUEST;

public class UserRequestHandler implements ReturnViewPathHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserRequestHandler.class);

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        if(paramMap.isEmpty()){
            throw new ClientException(BAD_REQUEST);
        }
        UserCreateRequest userCreateRequest = parseQueryString(paramMap);
        User createdUser = UserService.createUser(userCreateRequest);
        model.put("createdUser", createdUser);

        return "redirect:/index";
    }

    private UserCreateRequest parseQueryString(Map<String, String> paramMap) {

        return new UserCreateRequest(
                paramMap.getOrDefault("userId", ""),
                paramMap.getOrDefault("password", ""),
                paramMap.getOrDefault("name", ""),
                paramMap.getOrDefault("email", "")
        );
    }
}
