package com.example.projektopgave1.Model.UseCases;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.DatabaseHandlers.AftaleDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.KundeDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.MedarbejderDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.BehandlingDatabaseHandler;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import com.example.projektopgave1.Model.Entiteter.Kunde;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import com.example.projektopgave1.Model.Entiteter.Behandling;

import Utils.LoggerUtility;

public class UseCaseCalendar {
    private LocalDate currentDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private int selectedAppointmentId = -1;

    private final AftaleDatabaseHandler aftaleDatabaseHandler;
    private final KundeDatabaseHandler kundeDatabaseHandler;
    private final MedarbejderDatabaseHandler medarbejderDatabaseHandler;
    private final BehandlingDatabaseHandler behandlingDatabaseHandler;

    public static class AppointmentData {
        private int id;
        private String customerName;
        private String treatment;
        private String employee;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate date;

        public AppointmentData(int id, String customerName, String treatment, String employee,
                               LocalTime startTime, LocalTime endTime, LocalDate date) {
            this.id = id;
            this.customerName = customerName;
            this.treatment = treatment;
            this.employee = employee;
            this.startTime = startTime;
            this.endTime = endTime;
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getTreatment() {
            return treatment;
        }

        public String getEmployee() {
            return employee;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getDayOfWeek() {
            return date.getDayOfWeek().getValue();
        }
    }

    public UseCaseCalendar() {
        aftaleDatabaseHandler = new AftaleDatabaseHandler();
        kundeDatabaseHandler = new KundeDatabaseHandler();
        medarbejderDatabaseHandler = new MedarbejderDatabaseHandler();
        behandlingDatabaseHandler = new BehandlingDatabaseHandler();
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public String getFormattedDateRange() {
        LocalDate weekStart = currentDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        return weekStart.format(dateFormatter) + " - " + weekEnd.format(dateFormatter);
    }

    public void moveToToday() {
        currentDate = LocalDate.now();
    }

    public void moveToPreviousWeek() {
        currentDate = currentDate.minusWeeks(1);
    }

    public void moveToNextWeek() {
        currentDate = currentDate.plusWeeks(1);
    }

    public void setSelectedAppointmentId(int id) {
        this.selectedAppointmentId = id;
    }

    public int getSelectedAppointmentId() {
        return selectedAppointmentId;
    }

    public void clearSelectedAppointment() {
        this.selectedAppointmentId = -1;
    }

    public List<AppointmentData> getFilteredAppointments(boolean showJon, boolean showJoachim,
                                                         boolean showLasse, boolean showGabriel) {
        List<AppointmentData> filteredAppointments = new ArrayList<>();

        try {
            LocalDate weekStart = currentDate.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<Aftale> allAppointments = aftaleDatabaseHandler.getActiveAppointmentsInRange(weekStart, weekEnd);
            List<Medarbejder> allEmployees = medarbejderDatabaseHandler.getAll();

            Map<Integer, String> employeeNames = new HashMap<>();
            for (Medarbejder employee : allEmployees) {
                employeeNames.put(employee.getMedarbejderID(), employee.getNavn());
            }

            for (Aftale aftale : allAppointments) {
                if ("Aflyst".equals(aftale.getStatus())) {
                    continue;
                }

                String employeeName = employeeNames.getOrDefault(aftale.getMedarbejderID(), "Ukendt");

                boolean shouldShow = false;
                switch (employeeName) {
                    case "Jon":
                        shouldShow = showJon;
                        break;
                    case "Joachim":
                        shouldShow = showJoachim;
                        break;
                    case "Lasse":
                        shouldShow = showLasse;
                        break;
                    case "Gabriel":
                        shouldShow = showGabriel;
                        break;
                    default:
                        shouldShow = showJon && showJoachim && showLasse && showGabriel;
                }

                if (shouldShow) {
                    Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
                    Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());

                    String customerName = kunde != null ? kunde.getNavn() : "Ukendt kunde";
                    String treatmentName = behandling != null ? behandling.getBehandling() : "Ukendt behandling";

                    AppointmentData appointmentData = new AppointmentData(
                            aftale.getAftaleID(),
                            customerName,
                            treatmentName,
                            employeeName,
                            aftale.getStarttidspunkt().toLocalTime(),
                            aftale.getSluttidspunkt().toLocalTime(),
                            aftale.getStarttidspunkt().toLocalDate()
                    );

                    filteredAppointments.add(appointmentData);
                }
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved hentning af aftaler: " + e.getMessage());
        }

        return filteredAppointments;
    }

    public void createNewAppointment(String customerName, String treatment, String employee,
                                     LocalTime startTime, LocalTime endTime, LocalDate date) {
        try {
            int kundeId = findOrCreateCustomer(customerName);

            int medarbejderId = findEmployeeIdByName(employee);

            int behandlingId = findTreatmentIdByName(treatment);

            if (kundeId > 0 && medarbejderId > 0 && behandlingId > 0) {
                Aftale aftale = new Aftale();
                aftale.setKundeID(kundeId);
                aftale.setMedarbejderID(medarbejderId);
                aftale.setBehandlingID(behandlingId);
                aftale.setStarttidspunkt(LocalDateTime.of(date, startTime));
                aftale.setSluttidspunkt(LocalDateTime.of(date, endTime));
                aftale.setStatus("Booket");
                aftale.setOprettelsesdato(LocalDateTime.now());

                aftaleDatabaseHandler.create(aftale);
                LoggerUtility.logEvent("Ny aftale oprettet for " + customerName);
            } else {
                LoggerUtility.logError("Kunne ikke oprette aftale. Manglende kunde, medarbejder eller behandling.");
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved oprettelse af ny aftale: " + e.getMessage());
        }
    }

    private int findOrCreateCustomer(String customerName) throws DatabaseConnectionException {
        List<Kunde> customers = kundeDatabaseHandler.readAll();
        for (Kunde customer : customers) {
            if (customer.getNavn().equals(customerName)) {
                return customer.getKundeID();
            }
        }

        Kunde newCustomer = new Kunde(0, customerName, "", "", "");
        newCustomer = kundeDatabaseHandler.create(newCustomer);
        return newCustomer.getKundeID();
    }

    private int findEmployeeIdByName(String employeeName) throws DatabaseConnectionException {
        List<Medarbejder> employees = medarbejderDatabaseHandler.getAll();
        for (Medarbejder employee : employees) {
            if (employee.getNavn().equals(employeeName)) {
                return employee.getMedarbejderID();
            }
        }
        return -1;
    }

    private int findTreatmentIdByName(String treatmentName) throws DatabaseConnectionException {
        List<Behandling> treatments = behandlingDatabaseHandler.getAll();
        for (Behandling treatment : treatments) {
            if (treatment.getBehandling().equals(treatmentName)) {
                return treatment.getBehandlingID();
            }
        }
        return -1;
    }

    public void updateAppointment(int appointmentId, String customerName, String treatment, String employee,
                                  LocalTime startTime, LocalTime endTime, LocalDate date) {
        try {
            Aftale aftale = aftaleDatabaseHandler.getById(appointmentId);
            if (aftale == null) {
                LoggerUtility.logError("Kunne ikke finde aftale med ID: " + appointmentId);
                return;
            }

            int kundeId = findOrCreateCustomer(customerName);

            int medarbejderId = findEmployeeIdByName(employee);

            int behandlingId = findTreatmentIdByName(treatment);

            if (kundeId > 0 && medarbejderId > 0 && behandlingId > 0) {
                aftale.setKundeID(kundeId);
                aftale.setMedarbejderID(medarbejderId);
                aftale.setBehandlingID(behandlingId);
                aftale.setStarttidspunkt(LocalDateTime.of(date, startTime));
                aftale.setSluttidspunkt(LocalDateTime.of(date, endTime));

                aftaleDatabaseHandler.update(aftale);
                LoggerUtility.logEvent("Aftale opdateret med ID: " + appointmentId);
            } else {
                LoggerUtility.logError("Kunne ikke opdatere aftale. Manglende kunde, medarbejder eller behandling.");
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved opdatering af aftale: " + e.getMessage());
        }
    }

    public void deleteAppointment(int appointmentId) {
        try {
            Aftale aftale = aftaleDatabaseHandler.getById(appointmentId);
            if (aftale == null) {
                LoggerUtility.logError("Kunne ikke finde aftale med ID: " + appointmentId);
                return;
            }

            aftale.setStatus("Aflyst");
            aftaleDatabaseHandler.update(aftale);
            LoggerUtility.logEvent("Aftale annulleret med ID: " + appointmentId);
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved annullering af aftale: " + e.getMessage());
        }
    }

    public AppointmentData getAppointmentById(int appointmentId) {
        try {
            Aftale aftale = aftaleDatabaseHandler.getById(appointmentId);
            if (aftale == null) {
                return null;
            }

            Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
            Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());

            String employeeName = "Ukendt";
            Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());
            if (medarbejder != null) {
                employeeName = medarbejder.getNavn();
            }

            return new AppointmentData(
                    aftale.getAftaleID(),
                    kunde != null ? kunde.getNavn() : "Ukendt kunde",
                    behandling != null ? behandling.getBehandling() : "Ukendt behandling",
                    employeeName,
                    aftale.getStarttidspunkt().toLocalTime(),
                    aftale.getSluttidspunkt().toLocalTime(),
                    aftale.getStarttidspunkt().toLocalDate()
            );
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved hentning af aftale: " + e.getMessage());
            return null;
        }
    }

    public int timeToRow(LocalTime time) {
        int hour = time.getHour();
        int minutes = time.getMinute();
        return (hour - 8) * 2 + (minutes >= 30 ? 1 : 0);
    }

    public List<String> getAllEmployeeNames() {
        List<String> employeeNames = new ArrayList<>();
        try {
            List<Medarbejder> employees = medarbejderDatabaseHandler.getAll();
            for (Medarbejder employee : employees) {
                employeeNames.add(employee.getNavn());
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved hentning af medarbejdere: " + e.getMessage());
        }
        return employeeNames;
    }

    public List<String> getAllTreatmentNames() {
        List<String> treatmentNames = new ArrayList<>();
        try {
            List<Behandling> treatments = behandlingDatabaseHandler.getAll();
            for (Behandling treatment : treatments) {
                treatmentNames.add(treatment.getBehandling());
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved hentning af behandlinger: " + e.getMessage());
        }
        return treatmentNames;
    }

    public List<String> getAllCustomerNames() {
        List<String> customerNames = new ArrayList<>();
        try {
            List<Kunde> customers = kundeDatabaseHandler.readAll();
            for (Kunde customer : customers) {
                customerNames.add(customer.getNavn());
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved hentning af kunder: " + e.getMessage());
        }
        return customerNames;
    }

    public boolean isTimeSlotAvailable(String employeeName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            int employeeId = findEmployeeIdByName(employeeName);
            if (employeeId <= 0) {
                return false;
            }

            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

            List<Aftale> conflictingAppointments = aftaleDatabaseHandler.findConflictingAppointments(
                    employeeId, startDateTime, endDateTime);

            return conflictingAppointments.isEmpty();
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved kontrol af tidsslot: " + e.getMessage());
            return false;
        }
    }

    public List<LocalTime> getAvailableTimeSlots(String employeeName, LocalDate date, String treatmentName) {
        List<LocalTime> availableSlots = new ArrayList<>();
        try {
            int employeeId = findEmployeeIdByName(employeeName);
            int treatmentId = findTreatmentIdByName(treatmentName);

            if (employeeId <= 0 || treatmentId <= 0) {
                return availableSlots;
            }

            Behandling behandling = behandlingDatabaseHandler.getById(treatmentId);
            if (behandling == null) {
                return availableSlots;
            }

            int treatmentDuration = behandling.getVarighed();
            LocalTime openingTime = LocalTime.of(9, 0);
            LocalTime closingTime = LocalTime.of(18, 0);

            for (int hour = openingTime.getHour(); hour < closingTime.getHour(); hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalTime startTime = LocalTime.of(hour, minute);
                    LocalTime endTime = startTime.plusMinutes(treatmentDuration);

                    if (endTime.isAfter(closingTime)) {
                        continue;
                    }

                    if (isTimeSlotAvailable(employeeName, date, startTime, endTime)) {
                        availableSlots.add(startTime);
                    }
                }
            }

        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved hentning af ledige tider: " + e.getMessage());
        }

        return availableSlots;
    }
}