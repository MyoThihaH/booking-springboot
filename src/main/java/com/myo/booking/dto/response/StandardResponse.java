package com.myo.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> StandardResponse<T> success(String message, T data) {
        return new StandardResponse<>(true, message, data);
    }
    
    public static <T> StandardResponse<T> success(String message) {
        return new StandardResponse<>(true, message, null);
    }
    
    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>(true, "Success", data);
    }
    
    public static <T> StandardResponse<T> error(String message) {
        return new StandardResponse<>(false, message, null);
    }
}