package com.simpleshop.identity.infrastructure.config;

import com.simpleshop.cart.infrastructure.adapter.in.web.CartMergeAuthenticationSuccessHandler;
import com.simpleshop.identity.infrastructure.security.LoginRateLimitFilter;
import com.simpleshop.identity.infrastructure.security.RateLimitingAuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final CartMergeAuthenticationSuccessHandler cartMergeAuthenticationSuccessHandler;
    private final RateLimitingAuthenticationFailureHandler rateLimitingAuthenticationFailureHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;
    
    public SecurityConfig(CartMergeAuthenticationSuccessHandler cartMergeAuthenticationSuccessHandler,
                          RateLimitingAuthenticationFailureHandler rateLimitingAuthenticationFailureHandler,
                          LoginRateLimitFilter loginRateLimitFilter) {
        this.cartMergeAuthenticationSuccessHandler = cartMergeAuthenticationSuccessHandler;
        this.rateLimitingAuthenticationFailureHandler = rateLimitingAuthenticationFailureHandler;
        this.loginRateLimitFilter = loginRateLimitFilter;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/products/**", "/categories/**", "/cart/**").permitAll()
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/checkout/**", "/orders/**", "/shipments/track/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(cartMergeAuthenticationSuccessHandler)
                .failureHandler(rateLimitingAuthenticationFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .headers(headers -> {
                headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' https://cdn.jsdelivr.net; " +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' https://cdn.jsdelivr.net data:; " +
                    "object-src 'none'; " +
                    "frame-ancestors 'none'; " +
                    "base-uri 'self'; " +
                    "form-action 'self'"
                ));
                headers.referrerPolicy(referrer -> referrer.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                ));
                headers.frameOptions(frameOptions -> frameOptions.deny());
                headers.addHeaderWriter(new StaticHeadersWriter(
                    "Permissions-Policy",
                    "geolocation=(), microphone=(), camera=()"
                ));
            })
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
