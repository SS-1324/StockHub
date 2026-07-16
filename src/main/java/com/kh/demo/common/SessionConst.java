package com.kh.demo.common;

/*
    세션에 값을 저장할 때 사용하는 키를 한곳에 모아서 관리하는 클래스
    -> 여러파일에서 키를 사용하다보면 오타가 날 수 있고, 추후에 키 이름을 변경할 때
       모든 파일을 찾아서 고쳐야한다. 그래서 상수로 관리하면 편하다.
* */
public class SessionConst {

    public static final String LOGIN_MEMBER = "loginMember";

    // 정적상수만 관리하기위한 클래스이므로 객체생성 막아줌
    private SessionConst(){};
}
