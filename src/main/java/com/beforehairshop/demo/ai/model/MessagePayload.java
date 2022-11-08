package com.beforehairshop.demo.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayload {
    private String result;
    private BigInteger memberId;
    private BigInteger virtualMemberImageId;
}
