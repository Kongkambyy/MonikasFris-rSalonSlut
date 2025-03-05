package com.example.projektopgave1.Model.UseCases;
import com.example.projektopgave1.Model.DatabaseHandlers.AftaleDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.BehandlingDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.KundeDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.MedarbejderDatabaseHandler;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import Utils.LoggerUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
public class UseCaseOpretBooking {
    private AftaleDatabaseHandler aftaleDatabaseHandler;
    private KundeDatabaseHandler kundeDatabaseHandler;
    private MedarbejderDatabaseHandler medarbejderDatabaseHandler;
    private BehandlingDatabaseHandler behandlingDatabaseHandler;
    public UseCaseOpretBooking() {
        aftaleDatabaseHandler = new AftaleDatabaseHandler();
        kundeDatabaseHandler = new KundeDatabaseHandler();
        medarbejderDatabaseHandler = new MedarbejderDatabaseHandler();
        behandlingDatabaseHandler = new BehandlingDatabaseHandler();
    }
    public boolean createBooking(String customerName, String treatmentName, String employeeName,
                                 LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            int customerId = findOrCreateCustomer(customerName);
            int employeeId = findEmployeeId(employeeName);
            int treatmentId = findTreatmentId(treatmentName);
            if (customerId > 0 && employeeId > 0 && treatmentId > 0) {
                Aftale aftale = new Aftale();
                aftale.setKundeID(customerId);
                aftale.setMedarbejderID(employeeId);
                aftale.setBehandlingID(treatmentId);
                aftale.setStarttidspunkt(LocalDateTime.of(date, startTime));
                aftale.setSluttidspunkt(LocalDateTime.of(date, endTime));
                aftale.setStatus("Booket");
                aftale.setOprettelsesdato(LocalDateTime.now());
                aftaleDatabaseHandler.create(aftale);
                LoggerUtility.logEvent("Booking oprettet for " + customerName);
                return true;
            } else {
                LoggerUtility.logError("Booking oprettelse fejlede grundet manglende data");
                return false;
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved oprettelse af booking: " + e.getMessage());
            return false;
        }
    }
    private int findOrCreateCustomer(String customerName) throws Exception {
        List<com.example.projektopgave1.Model.Entiteter.Kunde> customers = kundeDatabaseHandler.readAll();
        for (com.example.projektopgave1.Model.Entiteter.Kunde kunde : customers) {
            if (kunde.getNavn().equalsIgnoreCase(customerName)) {
                return kunde.getKundeID();
            }
        }
        // Hvis kunden ikke findes, oprettes der en ny (her med tomme felter for de øvrige data)
        com.example.projektopgave1.Model.Entiteter.Kunde newCustomer =
                new com.example.projektopgave1.Model.Entiteter.Kunde(0, customerName, "", "", "");
        newCustomer = kundeDatabaseHandler.create(newCustomer);
        return newCustomer.getKundeID();
    }
    private int findEmployeeId(String employeeName) throws Exception {
        List<com.example.projektopgave1.Model.Entiteter.Medarbejder> employees = medarbejderDatabaseHandler.getAll();
        for (com.example.projektopgave1.Model.Entiteter.Medarbejder emp : employees) {
            if (emp.getNavn().equalsIgnoreCase(employeeName)) {
                return emp.getMedarbejderID();
            }
        }
        return -1;
    }
    private int findTreatmentId(String treatmentName) throws Exception {
        List<com.example.projektopgave1.Model.Entiteter.Behandling> treatments = behandlingDatabaseHandler.getAll();
        for (com.example.projektopgave1.Model.Entiteter.Behandling behand : treatments) {
            if (behand.getBehandling().equalsIgnoreCase(treatmentName)) {
                return behand.getBehandlingID();
            }
        }
        return -1;
    }
    public boolean isTimeSlotAvailable(String employeeName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            int employeeId = findEmployeeId(employeeName);
            if (employeeId <= 0) {
                return false;
            }
            List<Aftale> conflictingAppointments = aftaleDatabaseHandler.findConflictingAppointments(
                    employeeId, LocalDateTime.of(date, startTime), LocalDateTime.of(date, endTime)
            );
            return conflictingAppointments.isEmpty();
        } catch (Exception e) {
            LoggerUtility.logError("Fejl i kontrol af tidsslot: " + e.getMessage());
            return false;
        }
    }
    public List<String> getAllEmployeeNames() {
        try {
            List<com.example.projektopgave1.Model.Entiteter.Medarbejder> employees = medarbejderDatabaseHandler.getAll();
            List<String> names = new ArrayList<>();
            for (com.example.projektopgave1.Model.Entiteter.Medarbejder emp : employees) {
                names.add(emp.getNavn());
            }
            return names;
        } catch (Exception e) {
            LoggerUtility.logError("Fejl i hentning af medarbejdernavne: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public List<String> getAllTreatmentNames() {
        try {
            List<com.example.projektopgave1.Model.Entiteter.Behandling> treatments = behandlingDatabaseHandler.getAll();
            List<String> names = new ArrayList<>();
            for (com.example.projektopgave1.Model.Entiteter.Behandling behand : treatments) {
                names.add(behand.getBehandling());
            }
            return names;
        } catch (Exception e) {
            LoggerUtility.logError("Fejl i hentning af behandlingnavne: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public int getTreatmentDuration(String treatmentName) {
        try {
            List<com.example.projektopgave1.Model.Entiteter.Behandling> treatments = behandlingDatabaseHandler.getAll();
            for (com.example.projektopgave1.Model.Entiteter.Behandling behand : treatments) {
                if (behand.getBehandling().equalsIgnoreCase(treatmentName)) {
                    return behand.getVarighed();
                }
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl i hentning af behandlingens varighed: " + e.getMessage());
        }
        return 30;
    }
}
