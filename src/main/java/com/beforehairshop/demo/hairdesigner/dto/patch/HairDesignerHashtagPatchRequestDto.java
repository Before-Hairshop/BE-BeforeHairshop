package com.beforehairshop.demo.hairdesigner.dto.patch;


import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerHashtagPatchRequestDto {
    private String tag;

    public HairDesignerHashtag toEntity(HairDesignerProfile hairDesignerProfile) {
        return HairDesignerHashtag.builder()
                .hairDesignerProfile(hairDesignerProfile)
                .tag(tag)
                .status(1)
                .build();
    }
}
