package com.example.projektopgave1.Model.DatabaseHandlers;

import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import com.example.projektopgave1.Model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedarbejderDatabaseHandler {


    public Medarbejder create(Medarbejder medarbejder) throws DatabaseConnectionException {
        String sql = "INSERT INTO Medarbejdere (Navn, Brugernavn, Adgangskode, Nummer, Mail) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, medarbejder.getNavn());
            preparedStatement.setString(2, medarbejder.getBrugernavn());
            preparedStatement.setString(3, medarbejder.getAdgangskode());
            preparedStatement.setString(4, medarbejder.getNummer());
            preparedStatement.setString(5, medarbejder.getMail());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Oprettelse af den nye medarbejder fungerede ikke");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if(generatedKeys.next()) {
                    medarbejder.setMedarbejderID(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Kunne ikke oprette medarbejder, intet ID");
                }
            }

            return medarbejder;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i oprettelse af medarbejder: " + e.getMessage(), e);
        }
    }

    public Medarbejder getById(int id) throws DatabaseConnectionException {
        String sql = "SELECT * FROM Medarbejdere WHERE MedarbejderID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToMedarbejder(resultSet);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i hentning af medarbejder: " + e.getMessage(), e);
        }
    }

    public List<Medarbejder> getAll() throws DatabaseConnectionException {
        List<Medarbejder> medarbejdere = new ArrayList<>();
        String sql = "SELECT * FROM Medarbejdere";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medarbejdere.add(mapResultSetToMedarbejder(rs));
            }

            return medarbejdere;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i hentning af alle medarbejdere: " + e.getMessage(), e);
        }
    }

    public boolean update(Medarbejder medarbejder) throws DatabaseConnectionException {
        String sql = "UPDATE Medarbejdere SET Navn = ?, Brugernavn = ?, Adgangskode = ?, Nummer = ?, Mail = ? WHERE MedarbejderID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, medarbejder.getNavn());
            preparedStatement.setString(2, medarbejder.getBrugernavn());
            preparedStatement.setString(3, medarbejder.getAdgangskode());
            preparedStatement.setString(4, medarbejder.getNummer());
            preparedStatement.setString(5, medarbejder.getMail());
            preparedStatement.setInt(6, medarbejder.getMedarbejderID());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i opdatering af medarbejder: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) throws DatabaseConnectionException {
        String sql = "DELETE FROM Medarbejdere WHERE MedarbejderID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i sletning af medarbejder: " + e.getMessage(), e);
        }
    }

    public List<Medarbejder> findByName(String name) throws DatabaseConnectionException {
        List<Medarbejder> medarbejdere = new ArrayList<>();
        String sql = "SELECT * FROM Medarbejdere WHERE Navn LIKE ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, "%" + name + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    medarbejdere.add(mapResultSetToMedarbejder(resultSet));
                }
            }

            return medarbejdere;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i søgning efter medarbejdere: " + e.getMessage(), e);
        }
    }

    public boolean isUsernameAvailable(String username) throws DatabaseConnectionException {
        String sql = "SELECT COUNT(*) FROM Medarbejdere WHERE Brugernavn = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count == 0; // Returnerer true hvis ingen medarbejdere har dette brugernavn
                }
            }

            return true; //
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i kontrol af brugernavn: " + e.getMessage(), e);
        }
    }

    private Medarbejder mapResultSetToMedarbejder(ResultSet rs) throws SQLException {
        Medarbejder medarbejder = new Medarbejder();
        medarbejder.setMedarbejderID(rs.getInt("MedarbejderID"));
        medarbejder.setNavn(rs.getString("Navn"));
        medarbejder.setBrugernavn(rs.getString("Brugernavn"));
        medarbejder.setAdgangskode(rs.getString("Adgangskode"));
        medarbejder.setNummer(rs.getString("Nummer"));
        medarbejder.setMail(rs.getString("Mail"));
        return medarbejder;
    }
}