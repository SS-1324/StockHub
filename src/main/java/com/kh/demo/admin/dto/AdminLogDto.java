package com.kh.demo.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 관리자가 수행한 작업 이력을 전달
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogDto {

    private Long logId;
    private String adminId;
    private String adminNickname;
    private String actionType;
    private String targetType;
    private String targetId;
    private String detail;
    private LocalDateTime createAt;
    private String createAtStr;
}
