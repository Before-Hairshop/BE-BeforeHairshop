package com.beforehairshop.demo.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@AllArgsConstructor
public class LogFormat {
    private String level;
    private LocalDate date;
    private String result;
    private Integer responseCode;
    private String url;
    private String method;
    private String message;

    public static String makeLog(String level, LocalDate date, String result, Integer responseCode, String url, String method, String message) {
        LogFormat logFormat = new LogFormat(level, date, result, responseCode, url, method, message);
        return logFormat.toString();
    }

    public static String makeSuccessLog(Integer responseCode, String url, String method, String message) {
        LogFormat logFormat = new LogFormat("info", LocalDate.now(ZoneId.of("Asia/Seoul"))
                , "success", responseCode, url, method, message);
        return logFormat.toString();
    }

    public static String makeErrorLog(Integer responseCode, String url, String method, String message) {
        LogFormat logFormat = new LogFormat("error", LocalDate.now(ZoneId.of("Asia/Seoul"))
                , "fail", responseCode, url, method, message);
        return logFormat.toString();
    }

    @Override
    public String toString() {
        return "{\"level\":" + level
                + ", \"date\":" + date
                + ", \"result\":" + result
                + ", \"response_code\":"+ responseCode
                + ", \"url\":"+ url
                + ", \"method\":"+ method
                + ", \"message\":" + message + "}";
    }


}
