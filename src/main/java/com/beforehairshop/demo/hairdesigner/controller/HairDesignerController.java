package com.beforehairshop.demo.hairdesigner.controller;

import com.beforehairshop.demo.hairdesigner.dto.HairDesignerSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.service.HairDesignerService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "헤어 디자이너 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/hair-designers")
public class HairDesignerController {

    private final HairDesignerService hairDesignerService;

    @PostMapping()
    @Operation(summary = "헤어 디자이너 생성 API")
    public ResponseEntity<ResultDto> save(@RequestBody HairDesignerSaveRequestDto hairDesignerSaveRequestDto) {
        return hairDesignerService.save(hairDesignerSaveRequestDto);
    }



}
