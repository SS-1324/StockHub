<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>StockHub</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css?v=2">
</head>
<body>
    <header class="site-header">
        <div class="site-header-inner">
            <a class="logo" href="/">StockHub</a>
            <nav class="nav">
                <c:choose>
                    <c:when test="${not empty sessionScope.loginMember}">
                        <c:choose>
                            <c:when test="${not empty sessionScope.loginMember.profile}">
                                <img class="header-profile-image"
                                     width="36" height="36"
                                     style="width:36px; height:36px; border-radius:50%; object-fit:cover; flex-shrink:0;"
                                     src="${pageContext.request.contextPath}${sessionScope.loginMember.profile}"
                                     alt="${sessionScope.loginMember.nickname}님의 프로필 이미지">
                            </c:when>
                            <c:otherwise>
                                <span class="header-profile-image header-profile-placeholder"
                                      style="display:inline-block; width:36px; height:36px; border-radius:50%; flex-shrink:0;"
                                      role="img" aria-label="기본 프로필 이미지"></span>
                            </c:otherwise>
                        </c:choose>
                        <span>${sessionScope.loginMember.nickname}님</span>
                        <a href="/member/logout">로그아웃</a>
                    </c:when>
                    <c:otherwise>
                        <a href="/member/login">로그인</a>
                        <a href="/member/join">회원가입</a>
                    </c:otherwise>
                </c:choose>
            </nav>
        </div>
    </header>
    <main class="container">
