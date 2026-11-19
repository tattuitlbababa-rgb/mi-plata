package com.miplata.miplata.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Páginas que exigen sesión iniciada. Cada vez que migremos una página
    // nueva a Thymeleaf, se agrega su ruta acá.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AutenticacionInterceptor())
                .addPathPatterns("/inicio",
                        "/movimientos", "/movimientos/**",
                        "/ingresar", "/gastos",
                        "/categorias", "/categorias/**",
                        "/estadistica");
    }
}