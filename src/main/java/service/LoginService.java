package service;

import dto.LoginRequest;
import exception.ClientException;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.UserRepository;

import static domain.error.HttpClientError.UNAUTHORIZED;

public class LoginService {

    private static final UserRepository userRepository = new UserRepository();

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    public static User login(LoginRequest request){

        log.info("Login request: " + request.userId());

        User findUser = userRepository.findById(request.userId());

        if(findUser==null||!findUser.getPassword().equals(request.password())){
            log.info("Login failed");
            throw new ClientException(UNAUTHORIZED);
        }

        return findUser;
    }

}
