package kep.main.KEP.security;

import kep.main.KEP.service.UserManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    private final JwtAuthEntryPoint entryPoint;

    private final JwtRequestFilter jwtRequestFilter;

    private final UserManagerImpl userManagerImpl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }

    public WebSecurityConfig(JwtAuthEntryPoint entryPoint, JwtRequestFilter jwtRequestFilter, UserManagerImpl userManagerImpl) {
        this.entryPoint = entryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
        this.userManagerImpl = userManagerImpl;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/user/save");
    }

    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable().cors().and()
                .regexMatcher("(/kep/(.*))")

                .authorizeRequests(reg -> {
                    reg
                            .antMatchers("/kep/user/save").permitAll();
                })
                .exceptionHandling().authenticationEntryPoint(entryPoint)
                .and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userManagerImpl)
                .passwordEncoder(passwordEncoder());
    }
}
