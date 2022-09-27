package com.beforehairshop.demo.constant.member.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingFlagKind {
    ACTIVATION_CODE(1, "활성화"),
    DEACTIVATION_CODE(0, "비활성화");

    private Integer id;
    private String description;


}
