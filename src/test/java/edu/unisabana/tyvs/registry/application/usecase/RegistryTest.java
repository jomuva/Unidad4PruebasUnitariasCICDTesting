package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import edu.unisabana.tyvs.registry.infraestructure.persistence.RegistryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;;

public class RegistryTest {

    private RegistryRepositoryPort repo;
    private Registry registry;

    // PREPARAR: antes de cada prueba, crear H2 limpia
    @BeforeEach
    public void setup() throws Exception {
        String jdbc = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";
        repo = new RegistryRepository(jdbc);
        repo.initSchema(); // crear la tabla si no existe
        repo.deleteAll();  // limpiar datos previos
        registry = new Registry(repo);
    }

    // --- PRUEBA 1: persona válida ---
    @Test
    public void shouldRegisterValidPerson() throws Exception {
        // Arrange
        Person p = new Person("Ana", 100, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert: el resultado es VALID Y el dato quedó en la BD
        assertEquals(RegisterResult.VALID, result);
        assertTrue(repo.existsById(100));  // ← verificamos en la BD real
    }

    // --- PRUEBA 2: duplicado ---
    @Test
    public void shouldRejectDuplicatedId() throws Exception {
        // Arrange: registrar la primera vez
        Person primera = new Person("Ana",   100, 30, Gender.FEMALE, true);
        Person segunda = new Person("Carlos", 100, 25, Gender.MALE,   true);
        registry.registerVoter(primera); // guardamos el id 100

        // Act: intentar registrar otro con el mismo id
        RegisterResult result = registry.registerVoter(segunda);

        // Assert
        assertEquals(RegisterResult.DUPLICATED, result);
    }

    // --- PRUEBA 3: menor de edad ---
    @Test
    public void shouldRejectUnderage() throws Exception {
        Person joven = new Person("Luis", 200, 17, Gender.MALE, true);
        RegisterResult result = registry.registerVoter(joven);
        assertEquals(RegisterResult.UNDERAGE, result);
        assertFalse(repo.existsById(200)); // ← no se guardó en la BD
    }

    // --- PRUEBA 4: fallecido ---
    @Test
    public void shouldRejectDeadPerson() throws Exception {
        Person muerto = new Person("Eva", 300, 50, Gender.FEMALE, false);
        RegisterResult result = registry.registerVoter(muerto);
        assertEquals(RegisterResult.DEAD, result);
    }
}