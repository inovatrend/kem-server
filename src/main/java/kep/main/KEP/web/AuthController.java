package kep.main.KEP.web;

import kep.main.KEP.dto.UserCredentials;
import kep.main.KEP.security.JwtTokenUtil;
import kep.main.KEP.utils.UserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final UserUtils userUtils;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(UserUtils userUtils, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil) {
        this.userUtils = userUtils;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?>  login(@RequestBody UserCredentials userDetails) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword()));

        if (userUtils.isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.OK).body(jwtTokenUtil.generateToken(userDetails));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping(value = "/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session, HttpServletRequest request) {
        session.invalidate();
        request.getSession().invalidate();
        logger.info("User {} successfully logged out!", request.getRemoteUser());
    }
}
