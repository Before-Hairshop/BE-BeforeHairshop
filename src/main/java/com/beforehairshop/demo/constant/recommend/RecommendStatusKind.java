package com.beforehairshop.demo.constant.recommend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecommendStatusKind {
    REJECT(0,"거절"),
    WAIT(1,"대기 중"),
    ACCEPT(2, "수락"),
    DELETE(100,"삭제");

    private Integer id;
    private String description;
}
