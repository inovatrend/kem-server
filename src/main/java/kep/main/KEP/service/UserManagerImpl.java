package kep.main.KEP.service;

import kep.main.KEP.dao.UserRepository;
import kep.main.KEP.dto.UserDTO;
import kep.main.KEP.model.Permissions;
import kep.main.KEP.model.User;
import kep.main.KEP.utils.UserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserManagerImpl implements UserManager, UserDetailsService {
    private static final Logger logger = LogManager.getLogger(UserManagerImpl.class);
    private final UserRepository userRepository;
    private final UserUtils userUtils;

    @Value("${admin.password}")
    private String adminPassword;

    int strength = 10;
    BCryptPasswordEncoder bCryptPasswordEncoder =
            new BCryptPasswordEncoder(strength, new SecureRandom());

    public UserManagerImpl(UserRepository userRepository, UserUtils userUtils) {
        this.userRepository = userRepository;
        this.userUtils = userUtils;
    }

    @PostConstruct
    private void createAdminUser() {
        User adminUser = userRepository.findByUsername("admin");

        if (adminUser == null) {
            Set<Permissions> permissionsSet = new HashSet<>();
            permissionsSet.add(Permissions.ROLE_ADMIN);

            userRepository.save(new User(9999L, "admin",
                    bCryptPasswordEncoder.encode(adminPassword),
                    bCryptPasswordEncoder.encode(adminPassword),
                    "admin", "admin", permissionsSet));
        }
    }

    @Override
    public User getUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        return optionalUser.orElseGet(() -> (User) Optional.empty().get());
    }

    @Override
    public User getFullUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("User" + username + "not found!");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDTO save(User user) {
        try {
            Set<Permissions> permissionsSet = new HashSet<>();
            permissionsSet.add(Permissions.ROLE_USER);
            user.setPermissions(permissionsSet);

            encodeUserPasswordBCrypt(user);

            userRepository.save(user);
        } catch (Exception e) {
            logger.error("User not saved! {}", e.getMessage());
        }

        return userUtils.userDTOMapper(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDetails user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("User" + username + "not found!");
        }
    }

    private void encodeUserPasswordBCrypt(User user) {
        String encodePassword = bCryptPasswordEncoder.encode(user.getPassword());
        String encodeRePassword = bCryptPasswordEncoder.encode(user.getRepeatPassword());

        user.setPassword(encodePassword);
        user.setRepeatPassword(encodeRePassword);
    }
}
