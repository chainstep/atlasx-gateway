package com.chainstep.atlas.example;

import com.cartrust.atlas.ssikit.EnableAtlas;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "Example SSI service",
                version = "0.1",
                description = "API to show SSI usage"
        ),
        servers = {
                @Server(url = "/", description = "Default Server URL")
        }
)
@EnableAtlas
@SpringBootApplication
public class AtlasApplication {
    public static void main(String[] args) {
        SpringApplication.run(AtlasApplication.class, args);
    }
}
