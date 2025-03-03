package com.example.projektopgave1.Model.DatabaseHandlers;

import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.Entiteter.Behandling;
import com.example.projektopgave1.Model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BehandlingDatabaseHandler {

    public Behandling create(Behandling behandling) throws DatabaseConnectionException {
            String sql = "INSERT INTO Behandling (Behandling, Varighed, Pris) VALUES (?, ?, ?)";

            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                preparedStatement.setString(1, behandling.getBehandling());
                preparedStatement.setInt(2, behandling.getVarighed());
                preparedStatement.setInt(3, behandling.getPris());

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Oprettelse af den nye behandling fungerede ikke");
                }

                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if(generatedKeys.next()) {
                        behandling.setBehandlingID(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Kunne ikke oprette behandling, intet ID");
                    }
                }

                return behandling;
            } catch (SQLException e) {
                throw new DatabaseConnectionException("Fejl i oprettelse af behandling: " + e.getMessage(), e);
            }
        }

        public Behandling getById(int id) throws DatabaseConnectionException {
            String sql = "SELECT * FROM Behandling WHERE BehandlingID = ?";
            Behandling behandling = null;

            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        behandling = new Behandling(
                                resultSet.getInt("BehandlingID"),
                                resultSet.getString("Behandling"),
                                resultSet.getInt("Varighed"),
                                resultSet.getInt("Pris")
                        );
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException("Fejl i hentning af behandlinger: " + e.getMessage(), e);
            }
            return behandling;
        }

        public List<Behandling> getAll() throws DatabaseConnectionException {
            List<Behandling> behandlinger = new ArrayList<>();
            String sql = "SELECT * FROM Behandling";

            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                 Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(sql)) {

                while (resultSet.next()) {
                    Behandling behandling = new Behandling(
                            resultSet.getInt("BehandlingID"),
                            resultSet.getString("Behandling"),
                            resultSet.getInt("Varighed"),
                            resultSet.getInt("Pris")
                    );
                    behandlinger.add(behandling);
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException("Fejl i hentning af alle behandlinger: " + e.getMessage(), e);
            }
            return behandlinger;
        }

        public boolean update(Behandling behandling) throws DatabaseConnectionException {
            String sql = "UPDATE Behandling SET Behandling = ?, Varighed = ?, Pris = ? WHERE BehandlingID = ?";

            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, behandling.getBehandling());
                preparedStatement.setInt(2, behandling.getVarighed());
                preparedStatement.setInt(3, behandling.getPris());
                preparedStatement.setInt(4, behandling.getBehandlingID());

                int affectedRows = preparedStatement.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                throw new DatabaseConnectionException("Fejl i opdatering af behandlinger: " + e.getMessage(), e);
            }
        }

        public boolean delete(int id) throws DatabaseConnectionException {
            String sql = "DELETE FROM Behandling WHERE BehandlingID = ?";

            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, id);

                int affectedRows = preparedStatement.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                throw new DatabaseConnectionException("Fejl i sletning af behandling: " + e.getMessage(), e);
            }
        }
    }

