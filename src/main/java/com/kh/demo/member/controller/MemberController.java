package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/*
*   @Controller - 이 클래스는 요청을 받아서 화면(view)를 반환하는 mvc의 컨트롤다. + @Component
*   내부의 메서드가 String을 반환하면 spring.mvc.view.prefix=/WEB-INF/views/ + 반환값 + spring.mvc.view.suffix=.jsp 으로 조합해서
*   해당 jsp파일 찾아 랜더링한다. (return "member/login" -> /WEB-INF/views/member/login.jsp)
*
*   회원관련 화면이동, 폼처리를 전부 해당 컨트롤러가 담당.
* */

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/join")
    public String join(@ModelAttribute MemberDto memberDto,
                       @RequestParam(required = false) MultipartFile profileImage,
                       RedirectAttributes redirectAttributes){
        System.out.println(memberDto);
        System.out.println(profileImage);

        try {
            memberService.join(memberDto, profileImage);
        } catch (IOException e) {
            // RedirectAttributes.addFlashAttribute
            // 리다이렉트 후 딱 한번 다음 요청에서만 살아있는 데이터
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/join";
        }

        redirectAttributes.addFlashAttribute("joinSuccess", true);
        return "redirect:/member/login";
    }

    /*
    *   fetch를 이용한 비동기 요청(ajax)
    *  회원가입 폼에서 아이디 입력 후 중복확인을 누르는 순간,
    *  페이지 전체를 새로고침하지 않고 API를 호출해서 결과를 통해 부분적으로 DOM수정하여 보여준다
    *
    *  @ResponseBody -> 반환값을 View이름이 아니라 JSON 응답 "본문"으로 그대로 내려 보내겠다.
    * */
    @GetMapping("/checkId")
    @ResponseBody
    public ApiResponse<Boolean> checkId(@RequestParam String memberId){
        boolean duplicate = memberService.isMemberIdCheck(memberId);
        String message = duplicate ? "이미 사용중인 아이디 입니다." : "사용 가능한 아이디 입니다.";
        return ApiResponse.success(message, duplicate);
    }

    @PostMapping("/login")
    public String login(@RequestParam String memberId,
                        @RequestParam String memberPwd,
                        @RequestParam(required = false) String redirectURL,
                        RedirectAttributes redirectAttributes,
                        HttpSession session){
        try {
            MemberDto member = memberService.login(memberId, memberPwd);

            //로그인 성공 -> 세션에 로그인 정보 저장
            session.setAttribute(SessionConst.LOGIN_MEMBER, member);
        } catch(IllegalStateException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/login";
        }

        if(redirectURL != null && !redirectURL.isBlank()){
            return "redirect:" + redirectURL;
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session != null){
            session.invalidate(); //세션자체를 만료
        }

        return "redirect:/";
    }

    @PostMapping("/withdraw")
    public String withdraw(HttpSession session){
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        memberService.withdraw(loginMember.getMemberId());

        session.invalidate();
        return "redirect:/";
    }


    // ------------------- 화면 이동 요청
    @GetMapping("/join")
    public String joinForm(){return "member/join";}

    @GetMapping("/login")
    public String loginForm(){return "member/login";}

    @GetMapping("/mypage")
    public String mypage(){
        return "member/mypage";
    }
}
