package com.chainstep.atlas.example;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "service-offerings")
public class ServicesConfig {
    private List<Service> services = new ArrayList<Service>();

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public Service getServiceByName(String name) {
        return this.services.stream().filter(service -> service.getName().equals(name)).findFirst().orElse(null);
    }

    public List<String> getServiceNames() {
        return services.stream()
                .map(Service::getName).toList();
    }

    public static class Service {
        private String name;
        private String url;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }
}