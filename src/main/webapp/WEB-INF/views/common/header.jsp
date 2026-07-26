<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>커뮤니티</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
</head>
<body>
    <header class="site-header">
        <div class="site-header-inner">
            <a class="logo">커뮤니티</a>

            <!--
                로그인시 session에 loginMember값이 들어있음.
                해당 값의 존재유무에 따라서 UI를 분리.
            -->
            <nav class="nav">
                <c:choose>
                    <c:when test="${not empty sessionScope.loginMember}">
                        <span>${sessionScope.loginMember.nickname}님</span>
                        <a>글쓰기</a>
                        <a href="/member/mypage">마이페이지</a>
                        <a href="/member/logout">로그아웃</a>
                    </c:when>
                    <c:otherwise>
                        <%-- 페이지를 chart.jsp로 이동하기위해 작성함--%>
                        <a href="/hub/chart">hub</a>
                        <a href="/member/login">로그인</a>
                        <a href="/member/join">회원가입</a>
                    </c:otherwise>
                </c:choose>
            </nav>
        </div>
    </header>
    <main class="container">