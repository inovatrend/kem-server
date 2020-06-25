package kep.main.KEP.security;


import io.jsonwebtoken.ExpiredJwtException;
import kep.main.KEP.service.UserManagerImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {


    public static final String AUTHORIZATION = "Authorization";

    private final JwtTokenUtil jwtTokenUtil;

    private final UserManagerImpl userManagerImpl;

    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil, UserManagerImpl userManagerImpl) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userManagerImpl = userManagerImpl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

       final String tokenHeader = request.getHeader(AUTHORIZATION);
        String username = null;
        String jwtToken = null;

        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            jwtToken = tokenHeader.substring(7);

            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.debug("Error while getting username from token!", e);
            } catch (ExpiredJwtException e) {
                logger.debug("Token expired!", e);
            }
        } else {
            logger.warn("Jwt token doesn't start with Bearer String...");
        }

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = this.userManagerImpl.loadUserByUsername(username);


            if (jwtTokenUtil.validateToken(jwtToken, user)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
