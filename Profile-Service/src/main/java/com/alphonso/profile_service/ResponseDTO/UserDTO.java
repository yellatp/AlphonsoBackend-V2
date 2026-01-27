package com.alphonso.profile_service.ResponseDTO;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String role;
    private String password;
}
