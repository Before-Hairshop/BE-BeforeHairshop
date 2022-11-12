package com.beforehairshop.demo.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VirtualMemberImagePostResponseDto {

    @Schema(description = "VirtualMemberImage ID (PK)를 의미함.")
    private BigInteger id;

    @Schema(description = "VirtualMemberImage 를 저장하기 위한 PreSignedUrl")
    private String preSignedUrl;
}
