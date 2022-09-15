package com.beforehairshop.demo.auth;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 시큐리티 설정에서, loginProcessingUrl("/login");
 * /login 요청이 오면, 자동으로 UserDetailsService 타입으로 IoC되어 있는 loadUserByUsername 함수가 실행됨.
 */
@Service
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;


    /**
     * 리턴된 Member 엔티티는 UserDetails 객체의 매개변수로 들어가서 UserDetails 객체를 만들고,
     * UserDetails 객체는 Authentication 객체를 만든다.
     * 만들어진 Authentication 객체는 Security Session 내부로 들어가게 된다.
     * @param username the username identifying the user whose data is required.
     * @return 함수 종료시, @AuthenticationPrincipal 어노테이션이 만들어진다!!
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       // System.out.println("username : " + username);
        Member member = memberRepository.findByUsername(username);
        if (member != null) {
            return new PrincipalDetails(member);
        }

        return null;
    }
}

