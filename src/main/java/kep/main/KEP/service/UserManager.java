package kep.main.KEP.service;

import kep.main.KEP.dto.UserDTO;
import kep.main.KEP.model.User;


public interface UserManager {

     User getUser(Long userId);

     UserDTO save(User user);
}
