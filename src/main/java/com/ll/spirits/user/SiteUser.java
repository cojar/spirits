package com.ll.spirits.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
public class SiteUser {

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getValue()));
        return authorities;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username; // email 형식
    private String password;
    @Column(unique = true)
    private String nickname;
    @Enumerated(EnumType.STRING)
    private UserRole role;

    private LocalDate birthDate; // 생년월일 필드 추가

    public boolean isAdmin() {
        // 관리자 여부를 판별하는 로직을 구현
        // 예: 관리자라면 true, 일반 사용자라면 false 반환
        return "admin@gmail.com".equals(username) ||
                "admin1@gmail.com".equals(username) ||
                "admin2@gmail.com".equals(username) ||
                "insung5189@gmail.com".equals(username)
                ;
    }
}