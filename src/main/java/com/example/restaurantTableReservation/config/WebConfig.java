package com.example.restaurantTableReservation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UploadStoragePaths uploadStoragePaths;
    private final AdminSessionInterceptor adminSessionInterceptor;
    private final UserRoleInterceptor userRoleInterceptor;

    public WebConfig(UploadStoragePaths uploadStoragePaths,
                     AdminSessionInterceptor adminSessionInterceptor,
                     UserRoleInterceptor userRoleInterceptor) {
        this.uploadStoragePaths = uploadStoragePaths;
        this.adminSessionInterceptor = adminSessionInterceptor;
        this.userRoleInterceptor = userRoleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminSessionInterceptor)
                .addPathPatterns("/admin/**");
        registry.addInterceptor(userRoleInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**", "/uploads/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadStoragePaths.uploadsRoot().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
