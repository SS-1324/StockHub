package com.kh.demo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
    비동기요청시 JSON응답으로 전달해줄 공통 포맷 객체

    프론트에서 항상 {success, message, data} 형태로 응답이 온다는 가정을 할 수 있다면,
    매번 응답에따른 코드를 그때그때 생각해서 만들지않고 표준적으로 코드구성이 가능하기 때문에
    응답형식을 통일시켜준다.

    제네릭 사용하는 이유는 그때그때 응답데이터의 타입이 다르기 때문.
    중복결과(Boolean)응답 -> ApiResponse<Boolean>
    댓글 조회 응답(CommentDto) -> ApiResponse<CommentDto>
* */

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success; // 성공여부
    private String message; // 성공, 실패에 따른 메세지
    private T data;

    //성공 응답을 간단하게 만들기위해서 정적 메서드
    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, message, data);
    }

    //실패했을 경우 사용할 정적 메서드
    public static <T> ApiResponse<T> fail(String message){
        return new ApiResponse<>(false, message, null);
    }
}
