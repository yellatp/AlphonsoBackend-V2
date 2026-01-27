package com.alphonso.user_service.Exception;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.alphonso.user_service.DTO.ApiResponse;

@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType mediaType,
                                  Class converterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body instanceof ApiResponse) {
            return body;
        }

        int httpCode = response.getHeaders().getFirst("X-Status") != null
                ? Integer.parseInt(response.getHeaders().getFirst("X-Status"))
                : 200;

        String method = request.getMethod().name();

        String message = switch (method) {
            case "GET"    -> "Data fetched successfully";
            case "POST"   -> "Created successfully";
            case "PUT"    -> "Updated successfully";
            case "DELETE" -> "Deleted successfully";
            default       -> "Success";
        };

        return new ApiResponse<>(
                "success",
                httpCode,
                message,
                body
        );
    }
}
