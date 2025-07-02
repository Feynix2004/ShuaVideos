package org.shuavideos.config;

import org.shuavideos.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserService userService;


    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new Interceptor(userService))
                .addPathPatterns("/admin/**", "/authorize/**")
                .addPathPatterns("/luckyjourney/**")
                .excludePathPatterns("/luckyjourney/login/**","/luckyjourney/index/**","/luckyjourney/cdn/**","/luckyjourney/file/**");

    }


}
