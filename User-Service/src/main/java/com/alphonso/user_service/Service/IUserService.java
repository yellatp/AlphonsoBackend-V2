package com.alphonso.user_service.Service;

import com.alphonso.user_service.DTO.LoginResponse;
import com.alphonso.user_service.DTO.ResetPasswordRequest;
import com.alphonso.user_service.DTO.UserDTO;
import com.alphonso.user_service.DTO.GoogleRequest;
import com.alphonso.user_service.DTO.LoginRequest;
import com.alphonso.user_service.DTO.CreateUserRequest;

public interface IUserService {

	public LoginResponse register(String role, CreateUserRequest req);

	public LoginResponse loginWithGoogle(GoogleRequest req);

	public LoginResponse login(LoginRequest req);

	public void resetPassword(ResetPasswordRequest req);

	//public UserDTO validateTokenAndGetUser(String token);

	public UserDTO getUserByEmail(String email);

}
