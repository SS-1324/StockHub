<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>용어 사전</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
</head>
<body>
    <main class = "container">
        <!--제목-->
        <header>
            <h3>주식 용어 사전</h3>
            <p>
                주식이 낯설게만 느껴지는 여러분을 위해 준비한
                직관적이고 쉬운 설명으로 구성된 알짜배기 주식 용어 사전입니다
            </p>
        </header>
        <!--pageContext~~ : 자동으로 프로젝트의 루트 경로 갖고 오기 -->
        <!-- 카테고리 1번 -->
        <a href = "${pageContext.request.contextPath}/dictionary/category/trading" class = "category">
            <span class = "category-box">매매와 투자 행동</span>
        </a>
        <!-- 카테고리 2번 -->
        <a href = "${pageContext.request.contextPath}/dictionary/category/risk-management" class = "category">
            <span class = "category-box">투자자·자금·손익 관리</span>
        </a>
        <!-- 카테고리 3번 -->
        <a href = "${pageContext.request.contextPath}/dictionary/category/position" class = "category">
            <span class = "category-box">상품과 포지션</span>
        </a>
        <!-- 카테고리 4번 -->
        <a href = "${pageContext.request.contextPath}/dictionary/category/market" class = "category">
            <span class = "category-box">시장·지수·주문·거래 제도</span>
        </a>
        <!-- 카테고리 5번 -->
        <a href = "${pageContext.request.contextPath}/dictionary/category/fundamental" class = "category">
            <span class = "category-box">종목 정보와 기업 분석</span>
        </a>
        <!-- 카테고리 6번 -->
        <a href = "${pageContext.request.contextPath}/dictionary/category/chart" class = "category">
            <span class = "category-box">종목 정보와 기업 분석</span>
        </a>

    </main>

</body>