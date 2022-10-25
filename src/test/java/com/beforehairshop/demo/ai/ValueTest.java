package com.beforehairshop.demo.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueTest {

    @Value("${cloud.aws.region}")
    private String awsRegion;

    @Test
    public void Value_테스트() {
        //given
        System.out.println(awsRegion);
        //then
        assertThat(awsRegion).isNotEqualTo(null);
    }
}
