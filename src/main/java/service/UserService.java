package service;

import dto.UserCreateRequest;
import model.User;
import repository.UserRepository;

import java.util.List;

public class UserService {

    private static final UserRepository userRepository = new UserRepository();

    public static User createUser(UserCreateRequest request) {

        User user = new User(request.userId(), request.password(), request.name(), request.email());

        userRepository.save(user);

        return user;
    }

    public static List<User> getAllUsers() {
       return userRepository.findAll();
    }


}
