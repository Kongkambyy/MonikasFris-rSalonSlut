package com.example.projektopgave1.Model.UseCases;

import Utils.LoggerUtility;
import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.DatabaseHandlers.AftaleDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.BehandlingDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.KundeDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.MedarbejderDatabaseHandler;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import com.example.projektopgave1.Model.Entiteter.Behandling;
import com.example.projektopgave1.Model.Entiteter.Kunde;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UseCaseListeOversigt {

    private LocalDate currentDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final AftaleDatabaseHandler aftaleDatabaseHandler;
    private final KundeDatabaseHandler kundeDatabaseHandler;
    private final MedarbejderDatabaseHandler medarbejderDatabaseHandler;
    private final BehandlingDatabaseHandler behandlingDatabaseHandler;

    public UseCaseListeOversigt(AftaleDatabaseHandler aftaleDatabaseHandler, KundeDatabaseHandler kundeDatabaseHandler, MedarbejderDatabaseHandler medarbejderDatabaseHandler, BehandlingDatabaseHandler behandlingDatabaseHandler) {
        this.aftaleDatabaseHandler = aftaleDatabaseHandler;
        this.kundeDatabaseHandler = kundeDatabaseHandler;
        this.medarbejderDatabaseHandler = medarbejderDatabaseHandler;
        this.behandlingDatabaseHandler = behandlingDatabaseHandler;
    }

    public static class AppointmentData {
        private int id;
        private String customerId;
        private String customerName;
        private int employeeId;
        private String employeeName;
        private int treatmentId;
        private String treatmentName;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String status;

        public AppointmentData(int id, String customerId, String customerName, int employeeId, String employeeName,
                               int treatmentId, String treatmentName, LocalDate date, LocalTime startTime, LocalTime endTime, String status) {

            this.id = id;
            this.customerId = customerId;
            this.customerName = customerName;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.treatmentId = treatmentId;
            this.treatmentName = treatmentName;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
        }
    }

    public List<AppointmentData> getAllAppointments() {
        List<AppointmentData> appointments = new ArrayList<>();

        try {
            // Her henter vi alle aftalerne fra databasen
            List<Aftale> alleAftaler = aftaleDatabaseHandler.getAll();

            // Her henter vi de data fra databasen, som er relevante for listevisningen
            for (Aftale aftale : alleAftaler) {
                try {
                    // Kunde information
                    Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
                    String kundeNavn;
                    String kundeId;

                    if (kunde != null) {
                        kundeNavn = kunde.getNavn();
                        kundeId = String.valueOf(kunde.getKundeID());
                    } else {
                        kundeNavn = "Ukendt";
                        kundeId = "";
                    }

                    // Medarbejder information
                    Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());
                    String medarbejderNavn;
                    int medarbejderId;

                    if (medarbejder != null) {
                        medarbejderNavn = medarbejder.getNavn();
                        medarbejderId = medarbejder.getMedarbejderID();
                    } else {
                        medarbejderNavn = "Ukendt";
                        medarbejderId = 0;
                    }

                    // Behandlings information
                    Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());
                    String behandlingNavn;
                    int behandlingId;

                    if (behandling != null) {
                        behandlingNavn = behandling.getBehandling();
                        behandlingId = behandling.getBehandlingID();
                    } else {
                        behandlingNavn = "Ukendt";
                        behandlingId = 0;
                    }

                    // Opret AppointmentData objekt med de korrekte parametre
                    AppointmentData appointmentData = new AppointmentData(
                            aftale.getAftaleID(),
                            kundeId,
                            kundeNavn,
                            medarbejderId,
                            medarbejderNavn,
                            behandlingId,
                            behandlingNavn,
                            aftale.getStarttidspunkt().toLocalDate(),
                            aftale.getStarttidspunkt().toLocalTime(),
                            aftale.getSluttidspunkt().toLocalTime(),
                            aftale.getStatus()
                    );

                    // Tilføj til den korrekte liste
                    appointments.add(appointmentData);
                } catch (Exception e) {
                    LoggerUtility.logError("Fejl under konvertering af aftale " + aftale.getAftaleID() + ": " + e.getMessage());
                }
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved hentning af aftaler: " + e.getMessage());
        }
        return appointments;
    }
}