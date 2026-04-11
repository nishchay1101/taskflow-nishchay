package com.taskflow.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.taskflow.common.Patch;
import com.taskflow.common.PatchDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer patchModule() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Patch.class, new PatchDeserializer());
            builder.modulesToInstall(module);
        };
    }
}