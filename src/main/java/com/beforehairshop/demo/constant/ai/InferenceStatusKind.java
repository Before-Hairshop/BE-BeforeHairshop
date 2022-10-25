package com.beforehairshop.demo.constant.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InferenceStatusKind {
    FAIL(0,"fail","얼굴 인식 실패"),
    WAIT(1,"wait","대기 중"),
    SUCCESS(2,"success","추론 완료");

    private Integer id;
    private String title;
    private String description;
}
