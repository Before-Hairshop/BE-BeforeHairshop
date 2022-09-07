package com.beforehairshop.demo.hairdesigner.controller;

import com.beforehairshop.demo.hairdesigner.dto.HairDesignerSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.service.HairDesignerService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.message.ReusableMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@Tag(name = "헤어 디자이너 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/hair-designers")
public class HairDesignerController {

    private final HairDesignerService hairDesignerService;

    @GetMapping()
    @Operation(summary = "헤어 디자이너 상세 조회 API")
    public ResponseEntity<ResultDto> findOne(@RequestParam(value = "id", required = true) BigInteger id) {
        return hairDesignerService.findOne(id);
    }

    @GetMapping("list")
    @Operation(summary = "헤어 디자이너 목록 조회 API")
    public ResponseEntity<ResultDto> findMany(@PageableDefault(size = 5)Pageable pageable) {
        return hairDesignerService.findMany(pageable);
    }

    @PostMapping()
    @Operation(summary = "헤어 디자이너 생성 API")
    public ResponseEntity<ResultDto> save(@RequestBody HairDesignerSaveRequestDto hairDesignerSaveRequestDto) {
        return hairDesignerService.save(hairDesignerSaveRequestDto);
    }



}
