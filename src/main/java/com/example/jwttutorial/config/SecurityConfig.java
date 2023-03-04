package com.example.jwttutorial.config;

import com.example.jwttutorial.jwt.JwtAccessDeniedHandler;
import com.example.jwttutorial.jwt.JwtAuthenticationEntryPoint;
import com.example.jwttutorial.jwt.JwtSecurityConfig;
import com.example.jwttutorial.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // @PreAuthorize 애노테이션을 메소드 단위로 추가하기 위해 적용.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            TokenProvider tokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화 방식
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers(
                        "/h2-console/**"
                                    ,"/favicon.ico"
                );

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()

                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)// 서버에 인증에 필요한 정보를 저장하지 않기 때문에, disable 가능

                // h2-console을 위한 설정이라는데.. 어떻게 동작하는걸까?
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                // 세션 사용 x
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests() // httpServletRequest를 사용하는 요청들에 대한 접근 제한을 설정하겠다.
                .antMatchers("/api/hello").permitAll() // /api/hello에 대한 접근 요청은 인증 없이 접근을 허용하겠다.
                .antMatchers("/api/authenticate").permitAll() // 로그인은 세션이 없는 상태에서 요청 들어온다.
                .antMatchers("/api/signup").permitAll()
                .anyRequest().authenticated() // 나머지 요청들은 모두 인증되어야 한다.

                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
    }
}
