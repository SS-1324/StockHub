package com.kh.demo.setting.dto;

import lombok.*;

/*
 *   SettingDto : settings 테이블과 1:1로 대응되는 클래스
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SettingDto {

    private String memberId;
    private Boolean isStockPublic;
    private Boolean isWordTooltip;
    private Boolean isLightMode;
}
