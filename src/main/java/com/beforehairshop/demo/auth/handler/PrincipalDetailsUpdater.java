package com.beforehairshop.demo.auth.handler;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.member.domain.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class PrincipalDetailsUpdater {
    public static void setAuthenticationOfSecurityContext(Member member, String role) {
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority(role));

        Authentication authentication = new UsernamePasswordAuthenticationToken(new PrincipalDetails(member), null, updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
