package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;;
import static org.mockito.Mockito.*;

public class RegistryWithMockTest {

    private RegistryRepositoryPort repo;
    private Registry registry;

    @BeforeEach
    public void setUp() {
        // mock() crea un doble del repositorio, sin base de datos real
        repo = mock(RegistryRepositoryPort.class);
        registry = new Registry(repo);
    }

    // --- PRUEBA 1: duplicado detectado vía mock ---
    @Test
    public void shouldReturnDuplicatedWhenIdExists() throws Exception {
        // Arrange: le decimos al mock que id=7 YA existe
        when(repo.existsById(7)).thenReturn(true);
        Person p = new Person("Ana", 7, 25, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert resultado
        assertEquals(RegisterResult.DUPLICATED, result);
        // Assert interacción: nunca se llamó a save() porque era duplicado
        verify(repo, never()).save(anyInt(), anyString(), anyInt(), anyBoolean());
    }

    // --- PRUEBA 2: persona válida guardada correctamente ---
    @Test
    public void shouldCallSaveForValidPerson() throws Exception {
        // Arrange: el id 10 NO existe
        when(repo.existsById(10)).thenReturn(false);
        Person p = new Person("Mario", 10, 35, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert resultado
        assertEquals(RegisterResult.VALID, result);
        // Assert interacción: se llamó a save() exactamente una vez
        verify(repo, times(1)).save(eq(10), eq("Mario"), eq(35), eq(true));
    }

    // --- PRUEBA 3: excepción controlada ---
    @Test
    public void shouldHandleRepositoryException() throws Exception {
        // Arrange: simulamos un error de BDatos
        when(repo.existsById(5)).thenReturn(false);
        doThrow(new RuntimeException("BD caída")).when(repo)
                .save(anyInt(), anyString(), anyInt(), anyBoolean());
        Person p = new Person("Error", 5, 30, Gender.FEMALE, true);

        // Act + Assert: el sistema no explota, devuelve el error controlado
        try {
            registry.registerVoter(p);
        } catch (RuntimeException e) {
            assertEquals("BD caída", e.getMessage());
        }
    }
}