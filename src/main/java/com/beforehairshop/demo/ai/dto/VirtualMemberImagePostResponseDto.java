package com.beforehairshop.demo.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VirtualMemberImagePostResponseDto {

    private BigInteger id;
    private String preSignedUrl;
}
