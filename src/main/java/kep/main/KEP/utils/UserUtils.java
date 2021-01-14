package kep.main.KEP.utils;

import kep.main.KEP.dto.UserDTO;
import kep.main.KEP.model.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    public UserDTO userDTOMapper(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getRepeatPassword(),
                user.getFirstname(),
                user.getLastname(),
                user.getPermissions());
    }

    public User userMapper(UserDTO userDTO) {
        return new User(
                userDTO.getUserId(),
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getRepeatPassword(),
                userDTO.getFirstname(),
                userDTO.getLastname(),
                userDTO.getPermissions());
    }


    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }
}
