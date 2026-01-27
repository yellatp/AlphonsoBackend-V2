package com.alphonso.profile_service.OpenFeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.alphonso.profile_service.ResponseDTO.ApiResponse;
import com.alphonso.profile_service.ResponseDTO.UserDTO;


@FeignClient(name = "User-Service",
configuration = com.alphonso.profile_service.Security.FeignClientInterceptor.class)
public interface UserServiceClient {

    @GetMapping("/api/user/email/{email}")
    ApiResponse<UserDTO> getUserByEmail(@PathVariable String email);

}
