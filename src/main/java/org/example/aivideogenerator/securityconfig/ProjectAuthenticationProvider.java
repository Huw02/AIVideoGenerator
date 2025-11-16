package org.example.aivideogenerator.securityconfig;

import org.example.aivideogenerator.model.User;
import org.example.aivideogenerator.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ProjectAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserService userService;



    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String pwd = authentication.getCredentials().toString();
        System.out.println("loadUser kaldt: user=" + username);
        User customer = null;
        try {
            customer = userService.getUserByUsername(username);
        } catch (Exception ex) {
            System.out.println("Database fejl =" + ex.getMessage());
        }
        if (customer != null) {
            if (passwordEncoder.matches(pwd, customer.getPassword())) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(customer.getRole()));
                String roleString = customer.getRole(); // e.g. "ROLE_USER,ROLE_ADMIN"
                List<String> roles = Arrays.asList(roleString.split(","));
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role.trim()));
                }
                return new UsernamePasswordAuthenticationToken(username, pwd, authorities);
            } else {
                throw new BadCredentialsException("Invalid password");
            }
        } else {
            throw new BadCredentialsException("No user registered with this details!");
        }
    }

}


