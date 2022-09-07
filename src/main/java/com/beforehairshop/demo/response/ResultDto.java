package com.beforehairshop.demo.response;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

@Data
@Slf4j
public class ResultDto {

    private HttpStatus status;
    private Object result;

    public ResultDto() {
        this.status = HttpStatus.BAD_REQUEST;
        this.result = null;
    }

    public static ResponseEntity<ResultDto> makeResult(HttpStatus status, Object object) {
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        ResultDto message = new ResultDto();
        message.setStatus(status);
        message.setResult(object);

        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }
}
