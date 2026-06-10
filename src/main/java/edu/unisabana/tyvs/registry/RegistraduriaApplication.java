package edu.unisabana.tyvs.registry;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.application.usecase.Registry;
import edu.unisabana.tyvs.registry.infraestructure.persistence.RegistryRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RegistraduriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistraduriaApplication.class, args);
    }

    @Bean
    public RegistryRepositoryPort registryRepositoryPort() throws Exception {
        String jdbc = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";
        RegistryRepository repo = new RegistryRepository(jdbc);
        repo.initSchema();
        return repo;
    }

    @Bean
    public Registry registry(RegistryRepositoryPort port) {
        return new Registry(port);
    }
}