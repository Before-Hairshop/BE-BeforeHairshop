package com.beforehairshop.demo.hairdesigner.dto.response;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HairDesignerListGetResponseDto {
    private List<HairDesignerProfile> designerList;
}
