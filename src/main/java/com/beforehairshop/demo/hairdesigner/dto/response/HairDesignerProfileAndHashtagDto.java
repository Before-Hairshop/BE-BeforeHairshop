package com.beforehairshop.demo.hairdesigner.dto.response;

import com.beforehairshop.demo.hairdesigner.dto.HairDesignerHashtagDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerProfileAndHashtagDto {
    private HairDesignerProfileDto hairDesignerProfileDto;
    private List<HairDesignerHashtagDto> hairDesignerHashtagDtoList;
}
