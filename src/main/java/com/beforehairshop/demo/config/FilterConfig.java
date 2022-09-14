package com.beforehairshop.demo.config;

import com.beforehairshop.demo.oauth.ReturnFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ReturnFilter> returnFilter() {
        FilterRegistrationBean<ReturnFilter> bean = new FilterRegistrationBean<>(new ReturnFilter());
        bean.addUrlPatterns("/");
       // bean.addUrlPatterns("/oauth2/authorization/naver");

        bean.setOrder(0);
        return bean;
    }
}
