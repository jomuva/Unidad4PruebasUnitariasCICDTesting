package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;

public class Registry {
    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 121;

    private final RegistryRepositoryPort repo;

    public Registry(RegistryRepositoryPort repo) {
        this.repo = repo;
    }

    public RegisterResult registerVoter(Person p) throws Exception {
        if (p == null)                              return RegisterResult.INVALID;
        if (!p.isAlive())                           return RegisterResult.DEAD;
        if (p.getAge() < 0 || p.getAge() > EDAD_MAXIMA) return RegisterResult.INVALID_AGE;
        if (p.getAge() < EDAD_MINIMA)               return RegisterResult.UNDERAGE;
        if (repo.existsById(p.getId()))             return RegisterResult.DUPLICATED;
        repo.save(p.getId(), p.getName(), p.getAge(), p.isAlive());
        return RegisterResult.VALID;
    }
}