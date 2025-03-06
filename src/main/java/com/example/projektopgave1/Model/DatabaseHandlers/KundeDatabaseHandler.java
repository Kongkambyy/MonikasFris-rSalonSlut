package com.example.projektopgave1.Model.DatabaseHandlers;

import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.Entiteter.Kunde;
import com.example.projektopgave1.Model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KundeDatabaseHandler {


    public Kunde create(Kunde kunde) throws DatabaseConnectionException {
        String sql = "INSERT INTO kunde (Navn, Nummer, Mail, Adresse) VALUES (?,?,?,?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, kunde.getNavn());
            preparedStatement.setString(2, kunde.getNummer());
            preparedStatement.setString(3, kunde.getMail());
            preparedStatement.setString(4, kunde.getAdresse());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Oprettelse af kunde fejlet, ingen rækker blev påvirket.");
            }

            // Hent det auto-genererede ID
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    kunde.setKundeID(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Oprettelse af kunde fejlet, intet genereret ID fundet.");
                }
            }
            return kunde;

        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl ved oprettelse af kunde: " + e.getMessage(), e);
        }
    }


    public Kunde readById(int kundeID) throws DatabaseConnectionException {
        String sql = "SELECT * FROM kunde WHERE KundeID = ?";
        Kunde kunde = null;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, kundeID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    kunde = new Kunde(
                            resultSet.getInt("KundeID"),
                            resultSet.getString("Navn"),
                            resultSet.getString("Nummer"),
                            resultSet.getString("Mail"),
                            resultSet.getString("Adresse")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl ved hentning af kunde med ID " + kundeID + ": " + e.getMessage(), e);
        }
        return kunde;
    }


    public List<Kunde> readAll() throws DatabaseConnectionException {
        String sql = "SELECT * FROM kunde";
        List<Kunde> kunder = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Kunde kunde = new Kunde(
                        resultSet.getInt("KundeID"),
                        resultSet.getString("Navn"),
                        resultSet.getString("Nummer"),
                        resultSet.getString("Mail"),
                        resultSet.getString("Adresse")
                );
                kunder.add(kunde);
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl ved hentning af alle kunder: " + e.getMessage(), e);
        }
        return kunder;
    }

    public boolean update(Kunde kunde) throws DatabaseConnectionException {
        String sql = "UPDATE kunde SET Navn = ?, Nummer = ?, Mail = ?, Adresse = ? WHERE KundeID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, kunde.getNavn());
            preparedStatement.setString(2, kunde.getNummer());
            preparedStatement.setString(3, kunde.getMail());
            preparedStatement.setString(4, kunde.getAdresse());
            preparedStatement.setInt(5, kunde.getKundeID());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl ved opdatering af kunde med ID " + kunde.getKundeID() + ": " + e.getMessage(), e);
        }
    }

    public boolean delete(int kundeID) throws DatabaseConnectionException {
        String sql = "DELETE FROM kunde WHERE KundeID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, kundeID);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl ved sletning af kunde med ID " + kundeID + ": " + e.getMessage(), e);
        }
    }
}