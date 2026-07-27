package com.kh.demo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// JSON 응답 형식을 success, message, data로 통일
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success; // 요청 성공 여부
    private String message; // 화면에 전달할 메시지
    private T data; // 응답에 담을 데이터

    // 메시지 없는 성공 응답을 생성
    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(true, null, data);
    }

    // 메시지가 있는 성공 응답을 생성
    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, message, data);
    }

    // 실패 응답을 생성
    public static <T> ApiResponse<T> fail(String message){
        return new ApiResponse<>(false, message, null);
    }
}
