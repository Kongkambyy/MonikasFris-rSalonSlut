package com.example.projektopgave1.Model.DatabaseHandlers;

import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import com.example.projektopgave1.Model.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AftaleDatabaseHandler {


    public Aftale create(Aftale aftale) throws DatabaseConnectionException {
        String sql = "INSERT INTO Aftale (KundeID, MedarbejderID, BehandlingID, Starttidspunkt, Sluttidspunkt, Status, Oprettelsesdato) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, aftale.getKundeID());
            preparedStatement.setInt(2, aftale.getMedarbejderID());
            preparedStatement.setInt(3, aftale.getBehandlingID());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(aftale.getStarttidspunkt()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(aftale.getSluttidspunkt()));
            preparedStatement.setString(6, aftale.getStatus());
            preparedStatement.setDate(7, java.sql.Date.valueOf(aftale.getOprettelsesdato().toLocalDate()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Oprettelse af den nye aftale fungerede ikke");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    aftale.setAftaleID(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Kunne ikke oprette aftale, intet ID");
                }
            }

            return aftale;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i oprettelse af aftale: " + e.getMessage(), e);
        }
    }

    public Aftale getById(int id) throws DatabaseConnectionException {
        String sql = "SELECT * FROM Aftale WHERE AftaleID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return ResultHelper(resultSet);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i hentning af aftale: " + e.getMessage(), e);
        }
    }

    public List<Aftale> getAll() throws DatabaseConnectionException {
        List<Aftale> aftaler = new ArrayList<>();
        String sql = "SELECT * FROM Aftale";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {

            while (resultSet.next()) {
                aftaler.add(ResultHelper(resultSet));
            }

            return aftaler;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i hentning af alle aftaler: " + e.getMessage(), e);
        }
    }

    public Aftale update(Aftale aftale) throws DatabaseConnectionException {
        String sql = "UPDATE Aftale SET KundeID = ?, MedarbejderID = ?, BehandlingID = ?, " +
                "Starttidspunkt = ?, Sluttidspunkt = ?, Status = ? WHERE AftaleID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, aftale.getKundeID());
            preparedStatement.setInt(2, aftale.getMedarbejderID());
            preparedStatement.setInt(3, aftale.getBehandlingID());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(aftale.getStarttidspunkt()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(aftale.getSluttidspunkt()));
            preparedStatement.setString(6, aftale.getStatus());
            preparedStatement.setInt(7, aftale.getAftaleID());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                return aftale;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i opdatering af aftale: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) throws DatabaseConnectionException {
        String sql = "DELETE FROM Aftale WHERE AftaleID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i sletning af aftale: " + e.getMessage(), e);
        }
    }

    public List<Aftale> findByDate(LocalDate date) throws DatabaseConnectionException {
        List<Aftale> aftaler = new ArrayList<>();
        String sql = "SELECT * FROM Aftale WHERE DATE(Starttidspunkt) = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    aftaler.add(ResultHelper(resultSet));
                }
            }

            return aftaler;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i søgning efter aftaler på dato: " + e.getMessage(), e);
        }
    }

    public List<Aftale> findByEmployee(int medarbejderID) throws DatabaseConnectionException {
        List<Aftale> aftaler = new ArrayList<>();
        String sql = "SELECT * FROM Aftale WHERE MedarbejderID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, medarbejderID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    aftaler.add(ResultHelper(resultSet));
                }
            }

            return aftaler;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i søgning efter aftaler for medarbejder: " + e.getMessage(), e);
        }
    }

    public List<Aftale> findByCustomer(int kundeID) throws DatabaseConnectionException {
        List<Aftale> aftaler = new ArrayList<>();
        String sql = "SELECT * FROM Aftale WHERE KundeID = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, kundeID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    aftaler.add(ResultHelper(resultSet));
                }
            }

            return aftaler;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i søgning efter aftaler for kunde: " + e.getMessage(), e);
        }
    }

    public List<Aftale> findConflictingAppointments(int medarbejderID, LocalDateTime startTime, LocalDateTime endTime)
            throws DatabaseConnectionException {
        List<Aftale> aftaler = new ArrayList<>();

        String sql = "SELECT * FROM Aftale WHERE MedarbejderID = ? AND Status != 'Aflyst' AND " +
                "((Starttidspunkt <= ? AND Sluttidspunkt > ?) OR " +
                "(Starttidspunkt < ? AND Sluttidspunkt >= ?) OR " +
                "(Starttidspunkt >= ? AND Sluttidspunkt <= ?))";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, medarbejderID);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(endTime));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(startTime));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(endTime));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(startTime));
            preparedStatement.setTimestamp(6, Timestamp.valueOf(startTime));
            preparedStatement.setTimestamp(7, Timestamp.valueOf(endTime));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    aftaler.add(ResultHelper(resultSet));
                }
            }

            return aftaler;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i søgning efter konflikter: " + e.getMessage(), e);
        }
    }

    public List<Aftale> getActiveAppointmentsInRange(LocalDate startDate, LocalDate endDate)
            throws DatabaseConnectionException {
        List<Aftale> aftaler = new ArrayList<>();
        String sql = "SELECT * FROM Aftale WHERE DATE(Starttidspunkt) BETWEEN ? AND ? AND Status != 'Aflyst'";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    aftaler.add(ResultHelper(resultSet));
                }
            }

            return aftaler;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Fejl i hentning af aktive aftaler: " + e.getMessage(), e);
        }
    }

    private Aftale ResultHelper(ResultSet rs) throws SQLException {
        Aftale aftale = new Aftale();
        aftale.setAftaleID(rs.getInt("AftaleID"));
        aftale.setKundeID(rs.getInt("KundeID"));
        aftale.setMedarbejderID(rs.getInt("MedarbejderID"));
        aftale.setBehandlingID(rs.getInt("BehandlingID"));
        aftale.setStarttidspunkt(rs.getTimestamp("Starttidspunkt").toLocalDateTime());
        aftale.setSluttidspunkt(rs.getTimestamp("Sluttidspunkt").toLocalDateTime());
        aftale.setStatus(rs.getString("Status"));

        Date oprettelsesdatoDate = rs.getDate("Oprettelsesdato");
        if (oprettelsesdatoDate != null) {
            LocalDate oprettelsesdato = oprettelsesdatoDate.toLocalDate();
            aftale.setOprettelsesdato(oprettelsesdato.atStartOfDay());
        }

        return aftale;
    }
}