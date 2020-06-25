package kep.main.KEP.web;

import kep.main.KEP.dto.UserDTO;
import kep.main.KEP.model.User;
import kep.main.KEP.service.UserManager;
import kep.main.KEP.utils.UserUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserManager userManager;

    private final UserUtils userUtils;

    public UserController(UserManager userManager, UserUtils userUtils) {
        this.userManager = userManager;
        this.userUtils = userUtils;
    }

    @GetMapping("/get")
    private UserDTO getUser (@RequestParam Long userId) {

        User user = userManager.getUser(userId);

        return userUtils.UserDTOMapper(user);
    }

    @PostMapping("/save")
    private UserDTO save (@RequestBody UserDTO userDTO) {

        User user = userUtils.UserMapper(userDTO);

        return userManager.save(user);
    }
}
