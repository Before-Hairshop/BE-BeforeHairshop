package com.beforehairshop.demo.member.domain;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusKind {


    ABNORMAL(0,"비정상 상태","비정상 상태"),
    NORMAL(1,"일반 상태","일반 상태"),
    DELETE(100,"삭제","삭제");

    private Integer id;
    private String title;
    private String description;

}
