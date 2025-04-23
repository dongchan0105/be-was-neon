package service;

import db.Database;
import dto.UserCreateRequest;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserService {


    public static User createUser(UserCreateRequest request) {

        User user = new User(request.userId(), request.password(), request.name(), request.email());

        Database.addUser(user);

        return user;
    }

    public static List<User> getAllUsers() {
       return new ArrayList<>(Database.findAllUser());
    }


}
