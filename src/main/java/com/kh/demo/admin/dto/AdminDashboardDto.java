package com.kh.demo.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 관리자 대시보드의 핵심 집계 값을 전달
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {

    private Long memberCount;
    private Long boardCount;
    private Long commentCount;
    private Long glossaryCount;
    private Long pendingInquiryCount;
}
