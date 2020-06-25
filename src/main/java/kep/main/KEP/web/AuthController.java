package kep.main.KEP.web;

import kep.main.KEP.dto.UserCredentials;
import kep.main.KEP.security.JwtTokenUtil;
import kep.main.KEP.utils.UserUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

    @PostMapping(value = "/logout")
    public ResponseEntity<?>  logout(@RequestBody UserCredentials userDetails) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword()));

        if (userUtils.isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.OK).body(jwtTokenUtil.generateToken(userDetails));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
