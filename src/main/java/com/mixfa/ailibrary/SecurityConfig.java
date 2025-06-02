package com.mixfa.ailibrary;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;

import static com.vaadin.flow.server.HandlerHelper.isFrameworkInternalRequest;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends VaadinWebSecurity {
    private final SessionRegistry sessionRegistry;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll());
        http.sessionManagement(customizer -> {
            customizer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
            customizer.invalidSessionStrategy(((request, response) -> {
                final String redirectUrl = request.getContextPath() + "/login";

                if (isFrameworkInternalRequest(request.getServletPath(), request)) {
                    response.setHeader("Content-Type", "text/plain");
                    response.getWriter().write("Vaadin-Refresh: " + redirectUrl);
                } else {
                    response.sendRedirect(redirectUrl);
                }
            }));
            customizer.maximumSessions(1);
        });
        setOAuth2LoginPage(http, "/login");
    }

}
