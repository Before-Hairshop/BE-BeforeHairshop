package com.beforehairshop.demo.oauth;

import com.beforehairshop.demo.response.ResultDto;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import static com.beforehairshop.demo.response.ResultDto.makeResult;
import java.nio.charset.StandardCharsets;

public class ReturnFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        //create Json Object
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "OK");

        // finally output the json string
        out.print(jsonObject);

        // 홈페이지로 리다이렉트 될 때에는, json 객체를 리턴하도록 설정함.
        // chain.doFilter(request, response);
    }
}
