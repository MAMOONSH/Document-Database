package com.mammon.documntdb.authentication;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> loggingFilter() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthenticationFilter());

        registrationBean.addUrlPatterns("/v2/*");
        registrationBean.addUrlPatterns("/node/*");
        registrationBean.addUrlPatterns("/login");
        registrationBean.setOrder(1);

        return registrationBean;

    }

}