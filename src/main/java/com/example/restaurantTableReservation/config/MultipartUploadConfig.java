package com.example.restaurantTableReservation.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartUploadConfig {

    private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(20);
    private static final DataSize MAX_REQUEST_SIZE = DataSize.ofMegabytes(200);

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(MAX_FILE_SIZE);
        factory.setMaxRequestSize(MAX_REQUEST_SIZE);
        return factory.createMultipartConfig();
    }

    /**
     * Tomcat defaults (2MB post size, 10 multipart parts) reject restaurant register/edit
     * forms before Spring multipart settings apply. This customizer raises those limits.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatMultipartCustomizer() {
        int maxPostBytes = (int) MAX_REQUEST_SIZE.toBytes();
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(maxPostBytes);
            connector.setMaxPartCount(-1);
        });
    }
}
