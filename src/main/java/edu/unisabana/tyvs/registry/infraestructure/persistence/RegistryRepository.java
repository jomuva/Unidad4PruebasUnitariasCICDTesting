package edu.unisabana.tyvs.registry.infraestructure.persistence;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import java.sql.*;

public class RegistryRepository implements RegistryRepositoryPort {
    private final String jdbcUrl;

    public RegistryRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public void initSchema() throws Exception {
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             Statement s = c.createStatement()) {
            s.execute(
                    "CREATE TABLE IF NOT EXISTS voters (" +
                            "  id INT PRIMARY KEY," +
                            "  name VARCHAR(100)," +
                            "  age INT," +
                            "  alive BOOLEAN" +
                            ")"
            );
        }
    }

    @Override
    public void deleteAll() throws Exception {
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             Statement s = c.createStatement()) {
            s.execute("DELETE FROM voters");
        }
    }

    @Override
    public boolean existsById(int id) throws Exception {
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM voters WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    @Override
    public void save(int id, String name, int age, boolean alive) throws Exception {
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO voters (id, name, age, alive) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setInt(3, age);
            ps.setBoolean(4, alive);
            ps.execute();
        }
    }
}