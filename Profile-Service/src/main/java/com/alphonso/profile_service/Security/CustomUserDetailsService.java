package com.alphonso.profile_service.Security;

import lombok.extern.slf4j.Slf4j;
import com.alphonso.profile_service.OpenFeign.UserServiceClient;
import com.alphonso.profile_service.ResponseDTO.ApiResponse;
import com.alphonso.profile_service.ResponseDTO.UserDTO;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService  {

    private final UserServiceClient userClient;

    public CustomUserDetailsService(UserServiceClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    	log.info("Loading user details for email: {}", email);
        UserDTO userDto;
        ApiResponse<UserDTO> apiResp =
        		userClient.getUserByEmail(email);

        userDto = (apiResp != null) ? apiResp.getData() : null;
        if (userDto == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + userDto.getRole())
        );

        return new org.springframework.security.core.userdetails.User(
                userDto.getEmail(),
                userDto.getPassword(),
                authorities
        );
    }
}
