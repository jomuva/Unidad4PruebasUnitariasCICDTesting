package edu.unisabana.tyvs.registry.delivery.rest;

import edu.unisabana.tyvs.registry.application.usecase.Registry;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegistryController {
    private final Registry registry;

    public RegistryController(Registry registry) {
        this.registry = registry;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody PersonRequest req) {
        try {
            Person p = new Person(req.name, req.id, req.age,
                    Gender.valueOf(req.gender), req.alive);
            return ResponseEntity.ok(registry.registerVoter(p).name());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ERROR: " + e.getMessage());
        }
    }
}