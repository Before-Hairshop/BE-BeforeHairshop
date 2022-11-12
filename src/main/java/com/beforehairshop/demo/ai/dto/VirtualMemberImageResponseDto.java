package com.beforehairshop.demo.ai.dto;

import com.beforehairshop.demo.ai.domain.VirtualMemberImage;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigInteger;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VirtualMemberImageResponseDto {
    private BigInteger id;
    private BigInteger memberId;
    private String imageUrl;
    private Integer inferenceStatus;    // 2(성공), 1(대기), 0(실패)
    private Date createDate;
    private Date updateDate;
    private int status;

    public VirtualMemberImageResponseDto(VirtualMemberImage virtualMemberImage) {
        this.id = virtualMemberImage.getId();
        this.memberId = virtualMemberImage.getMember().getId();
        this.imageUrl = virtualMemberImage.getImageUrl();
        this.inferenceStatus = virtualMemberImage.getInferenceStatus();
        this.createDate = virtualMemberImage.getCreateDate();
        this.updateDate = virtualMemberImage.getUpdateDate();
        this.status = virtualMemberImage.getStatus();
    }
}
