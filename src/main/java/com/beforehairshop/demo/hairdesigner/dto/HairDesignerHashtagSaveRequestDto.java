package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HairDesignerHashtagSaveRequestDto {
    private String tag;

    public HairDesignerHashtag toEntity(Member hairDesigner) {
        return HairDesignerHashtag.builder()
                .tag(tag)
                .build();
    }
}
