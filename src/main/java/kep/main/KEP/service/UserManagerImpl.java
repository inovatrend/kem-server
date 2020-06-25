package kep.main.KEP.service;

import kep.main.KEP.dao.UserRepository;
import kep.main.KEP.dto.UserDTO;
import kep.main.KEP.model.User;
import kep.main.KEP.utils.UserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class UserManagerImpl implements UserManager, UserDetailsService {
    private static Logger logger = LogManager.getLogger(UserManagerImpl.class);
    private final UserRepository userRepository;
    private final UserUtils userUtils;

    public UserManagerImpl(UserRepository userRepository, UserUtils userUtils) {
        this.userRepository = userRepository;
        this.userUtils = userUtils;
    }

    @Override
    public User getUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        return optionalUser.orElseGet(() -> (User) Optional.empty().get());
    }

    @Override
    public UserDTO save(User user) {
        try {
            encodeUserPasswordBCrypt(user);

            userRepository.save(user);
        } catch (Exception e) {
            logger.error("User not saved!", e);
        }

        return userUtils.UserDTOMapper(user);
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
        int strength = 10;
        BCryptPasswordEncoder bCryptPasswordEncoder =
                new BCryptPasswordEncoder(strength, new SecureRandom());
        String encodePassword = bCryptPasswordEncoder.encode(user.getPassword());
        String encodeRePassword = bCryptPasswordEncoder.encode(user.getRepeatPassword());

        user.setPassword(encodePassword);
        user.setRepeatPassword(encodeRePassword);
    }
}
