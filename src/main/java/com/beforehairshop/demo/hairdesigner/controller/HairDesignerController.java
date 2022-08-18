package com.beforehairshop.demo.hairdesigner.controller;

import com.beforehairshop.demo.hairdesigner.dto.HairDesignerSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.service.HairDesignerService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.message.ReusableMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "헤어 디자이너 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/hair-designers")
public class HairDesignerController {

    private final HairDesignerService hairDesignerService;

    @GetMapping("{id}")
    @Operation(summary = "헤어 디자이너 상세 조회 API")
    public ResponseEntity<ResultDto> findOne(@PathVariable Long id) {
        return hairDesignerService.findOne(id);
    }

    @PostMapping()
    @Operation(summary = "헤어 디자이너 생성 API")
    public ResponseEntity<ResultDto> save(@RequestBody HairDesignerSaveRequestDto hairDesignerSaveRequestDto) {
        return hairDesignerService.save(hairDesignerSaveRequestDto);
    }



}
