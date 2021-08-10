package com.qapel.rfid;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Set up directory structure for resources
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**", "/sound/**", "/js/**", "/css/**")
                .addResourceLocations("classpath:/image/", "classpath:/sound/", "classpath:/js/", "classpath:/css/")
                .setCachePeriod(0);
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/station").setViewName("station");
        registry.addViewController("/templates/readerConfiguration").setViewName("templates/readerConfiguration");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
    }
}