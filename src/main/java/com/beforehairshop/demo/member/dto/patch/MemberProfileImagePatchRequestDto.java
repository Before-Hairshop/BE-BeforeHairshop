package com.beforehairshop.demo.member.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileImagePatchRequestDto {
    private Integer frontImageFlag;
    private Integer sideImageFlag;
    private Integer backImageFlag;
    private Integer addDesiredHairstyleImageCount;
    private List<String> deleteDesiredImageUrlList;
}
