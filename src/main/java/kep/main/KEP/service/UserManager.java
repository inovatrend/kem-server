package kep.main.KEP.service;

import kep.main.KEP.dto.UserDTO;
import kep.main.KEP.model.User;

import java.util.List;


public interface UserManager {

     User getUser(Long userId);

     User getFullUserByUsername(String username);

     List<User> getAllUsers();

     UserDTO save(User user);
}
