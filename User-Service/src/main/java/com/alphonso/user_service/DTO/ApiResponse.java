package com.alphonso.user_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String status;     
    private int statusCode;      
    private String message;
    private T data;
    

    public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
        return new ApiResponse<>("success", statusCode, message, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", 200, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", 200, "Success", data);
    }


    public static <T> ApiResponse<T> error(int statusCode, String message, T errors) {
        return new ApiResponse<>("error", statusCode, message, errors);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", 400, message, null);
    }
}
