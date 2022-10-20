package com.beforehairshop.demo.config;

import com.beforehairshop.demo.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@EnableWebSecurity  // 스프링 시큐리티 필터(SecurityConfig.java) 가 스프링 필터체인에 등록된다.
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)  // secured 어노테이션 활성화 + PreAuthorize 어노테이션 활성화
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    /**
     * *@Bean 으로 등록하면, 해당 메서드의 리턴되는 객체를 IoC로 등록해준다. 즉 의존성 주입이 된다
     */
    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }


    // h2-console 접속하기 위함이다.
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/h2-console/**");
    }

    /**
     * <h3>URL 별로 권한 설정</h3>
     * <pre>/user : (인증) 로그인 한 사람만 접근 가능</pre>
     * <pre>/manager : (인증 + 권한) 로그인 + 권한이 ADMIN 이나 MANAGER 여야 접근 가능</pre>
     * <pre>/admin : (인증 + 권한) 로그인 + 권한이 ADMIN 이여야 함.</pre>
     * 나머지는 그냥 권한 없이 접근 가능함.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

//        http
//                .logout(logout -> logout
//                        .logoutUrl("/api/v1/oauth/logout")
//                        .addLogoutHandler(new SecurityContextLogoutHandler())
//                );

        http.authorizeRequests()
                .antMatchers("/api/v1/users/**").authenticated()    //인증만 되면 들어갈 수 있는 주소!
                .antMatchers("/api/v1/designers/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_HAIRDESIGNER')")
                .antMatchers("/api/v1/admin/**").access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll()

//                .and()
//                .formLogin()
//                .loginPage("/loginForm")
//                .loginProcessingUrl("/login")  // /login 주소가 호출이 되면, 시큐리티가 낚아채서 대신 로그인을 진행해준다.
//                .defaultSuccessUrl("/")

                .and()
                .oauth2Login()
                //.loginPage("/loginForm")
                .userInfoEndpoint()
                .userService(principalOauth2UserService);
        // .usernameParameter("username2")   : username 파라미터 이름이 username2 이거나 다른 이름일 때 이렇게 사용함.
    }
}
