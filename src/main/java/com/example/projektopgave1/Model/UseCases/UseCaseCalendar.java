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
    // Aktuel dato, som bruges som reference for kalendervisning
    private LocalDate currentDate = LocalDate.now();
    // Formatter til at vise datoer i dansk format
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    // ID for den valgte aftale i kalendervisningen
    private int selectedAppointmentId = -1;

    // Database handlers til at kommunikere med de forskellige databaser
    private final AftaleDatabaseHandler aftaleDatabaseHandler;
    private final KundeDatabaseHandler kundeDatabaseHandler;
    private final MedarbejderDatabaseHandler medarbejderDatabaseHandler;
    private final BehandlingDatabaseHandler behandlingDatabaseHandler;

    // Indre klasse, der bruges til at repræsentere aftaler i UI'en
    public static class AppointmentData {
        // Grundlæggende information om en aftale til brug i kalendervisningen
        private int id;
        private String customerName;
        private String treatment;
        private String employee;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate date;

        // Konstruktør til at oprette et nyt AppointmentData objekt
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

        // Get-metoder til at tilgå aftaledata
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

        // Returnerer ugedagen som et tal (1=mandag, 7=søndag)
        public int getDayOfWeek() {
            return date.getDayOfWeek().getValue();
        }
    }

    // Konstruktør, der initialiserer alle database handlers
    public UseCaseCalendar() {
        aftaleDatabaseHandler = new AftaleDatabaseHandler();
        kundeDatabaseHandler = new KundeDatabaseHandler();
        medarbejderDatabaseHandler = new MedarbejderDatabaseHandler();
        behandlingDatabaseHandler = new BehandlingDatabaseHandler();
    }

    // Henter den aktuelle dato
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    // Formatterer datointerval for den aktuelle uge (mandag-søndag)
    public String getFormattedDateRange() {
        // Find ugens første dag (mandag)
        LocalDate weekStart = currentDate.with(DayOfWeek.MONDAY);
        // Find ugens sidste dag (søndag)
        LocalDate weekEnd = weekStart.plusDays(6);
        // Returnerer formateret datointerval, fx "01-05-2024 - 07-05-2024"
        return weekStart.format(dateFormatter) + " - " + weekEnd.format(dateFormatter);
    }

    // Sætter den aktuelle dato til i dag
    public void moveToToday() {
        currentDate = LocalDate.now();
    }

    // Går en uge tilbage i kalenderen
    public void moveToPreviousWeek() {
        currentDate = currentDate.minusWeeks(1);
    }

    // Går en uge frem i kalenderen
    public void moveToNextWeek() {
        currentDate = currentDate.plusWeeks(1);
    }

    // Sætter ID for den valgte aftale
    public void setSelectedAppointmentId(int id) {
        this.selectedAppointmentId = id;
    }

    // Henter ID for den valgte aftale
    public int getSelectedAppointmentId() {
        return selectedAppointmentId;
    }

    // Nulstiller den valgte aftale
    public void clearSelectedAppointment() {
        this.selectedAppointmentId = -1;
    }

    // Henter filtrerede aftaler baseret på valgte medarbejdere
    public List<AppointmentData> getFilteredAppointments(boolean showJon, boolean showJoachim,
                                                         boolean showLasse, boolean showGabriel) {
        List<AppointmentData> filteredAppointments = new ArrayList<>();

        try {
            // Find start- og slutdato for den aktuelle uge
            LocalDate weekStart = currentDate.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            // Hent alle aktive aftaler i den valgte uge fra databasen
            List<Aftale> allAppointments = aftaleDatabaseHandler.getActiveAppointmentsInRange(weekStart, weekEnd);
            // Hent alle medarbejdere fra databasen
            List<Medarbejder> allEmployees = medarbejderDatabaseHandler.getAll();

            // Opret et map for hurtig opslag af medarbejdernavne ud fra ID
            Map<Integer, String> employeeNames = new HashMap<>();
            for (Medarbejder employee : allEmployees) {
                employeeNames.put(employee.getMedarbejderID(), employee.getNavn());
            }

            // Gennemgå alle aftaler og filtrer dem baseret på medarbejdervalg
            for (Aftale aftale : allAppointments) {
                // Spring over aflyste aftaler
                if ("Aflyst".equals(aftale.getStatus())) {
                    continue;
                }

                // Find medarbejderens navn baseret på ID
                String employeeName = employeeNames.getOrDefault(aftale.getMedarbejderID(), "Ukendt");

                // Tjek om denne medarbejders aftaler skal vises
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
                        // Hvis alle er valgt, vis også ukendte medarbejdere
                        shouldShow = showJon && showJoachim && showLasse && showGabriel;
                }

                // Hvis denne medarbejders aftaler skal vises, tilføj til listen
                if (shouldShow) {
                    // Hent kundeinformation
                    Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
                    // Hent behandlingsinformation
                    Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());

                    // Sæt standardværdier hvis data ikke kunne findes
                    String customerName = kunde != null ? kunde.getNavn() : "Ukendt kunde";
                    String treatmentName = behandling != null ? behandling.getBehandling() : "Ukendt behandling";

                    // Opret et AppointmentData objekt med relevante informationer
                    AppointmentData appointmentData = new AppointmentData(
                            aftale.getAftaleID(),
                            customerName,
                            treatmentName,
                            employeeName,
                            aftale.getStarttidspunkt().toLocalTime(),
                            aftale.getSluttidspunkt().toLocalTime(),
                            aftale.getStarttidspunkt().toLocalDate()
                    );

                    // Tilføj aftalen til den filtrerede liste
                    filteredAppointments.add(appointmentData);
                }
            }
        } catch (DatabaseConnectionException e) {

            // Log fejlen hvis der opstår problemer med databasen
            LoggerUtility.logError("Fejl ved hentning af aftaler: " + e.getMessage());
        }

        return filteredAppointments;
    }

    // Opretter en ny aftale i systemet
    public void createNewAppointment(String customerName, String treatment, String employee,
                                     LocalTime startTime, LocalTime endTime, LocalDate date) {
        try {
            // Find eller opret kundens ID
            int kundeId = findOrCreateCustomer(customerName);

            // Find medarbejderens ID
            int medarbejderId = findEmployeeIdByName(employee);

            // Find behandlingens ID
            int behandlingId = findTreatmentIdByName(treatment);

            // Hvis alle IDs blev fundet, opret aftalen
            if (kundeId > 0 && medarbejderId > 0 && behandlingId > 0) {
                Aftale aftale = new Aftale();
                aftale.setKundeID(kundeId);
                aftale.setMedarbejderID(medarbejderId);
                aftale.setBehandlingID(behandlingId);
                aftale.setStarttidspunkt(LocalDateTime.of(date, startTime));
                aftale.setSluttidspunkt(LocalDateTime.of(date, endTime));
                aftale.setStatus("Booket");
                aftale.setOprettelsesdato(LocalDateTime.now());

                // Gem aftalen i databasen
                aftaleDatabaseHandler.create(aftale);
                LoggerUtility.logEvent("Ny aftale oprettet for " + customerName);
            } else {
                // Log fejl hvis nogle nødvendige data mangler
                LoggerUtility.logError("Kunne ikke oprette aftale. Manglende kunde, medarbejder eller behandling.");
            }
        } catch (Exception e) {
            // Log generelle fejl under oprettelsen
            LoggerUtility.logError("Fejl ved oprettelse af ny aftale: " + e.getMessage());
        }
    }

    // Finder kundens ID i databasen, eller opretter en ny kunde hvis den ikke findes
    private int findOrCreateCustomer(String customerName) throws DatabaseConnectionException {
        // Hent alle kunder fra databasen
        List<Kunde> customers = kundeDatabaseHandler.readAll();
        // Tjek om kunden allerede findes
        for (Kunde customer : customers) {
            if (customer.getNavn().equals(customerName)) {
                return customer.getKundeID();
            }
        }

        // Hvis kunden ikke findes, opret en ny med tomme felter
        Kunde newCustomer = new Kunde(0, customerName, "", "", "");
        newCustomer = kundeDatabaseHandler.create(newCustomer);
        return newCustomer.getKundeID();
    }

    // Finder medarbejderens ID baseret på navn
    private int findEmployeeIdByName(String employeeName) throws DatabaseConnectionException {
        // Hent alle medarbejdere fra databasen
        List<Medarbejder> employees = medarbejderDatabaseHandler.getAll();
        // Find den medarbejder, der matcher navnet
        for (Medarbejder employee : employees) {
            if (employee.getNavn().equals(employeeName)) {
                return employee.getMedarbejderID();
            }
        }
        // Returner -1 hvis medarbejderen ikke blev fundet
        return -1;
    }

    // Finder behandlingens ID baseret på navn
    private int findTreatmentIdByName(String treatmentName) throws DatabaseConnectionException {
        // Hent alle behandlinger fra databasen
        List<Behandling> treatments = behandlingDatabaseHandler.getAll();
        // Find den behandling, der matcher navnet
        for (Behandling treatment : treatments) {
            if (treatment.getBehandling().equals(treatmentName)) {
                return treatment.getBehandlingID();
            }
        }
        // Returner -1 hvis behandlingen ikke blev fundet
        return -1;
    }

    // Opdaterer en eksisterende aftale
    public void updateAppointment(int appointmentId, String customerName, String treatment, String employee,
                                  LocalTime startTime, LocalTime endTime, LocalDate date) {
        try {
            // Hent den eksisterende aftale fra databasen
            Aftale aftale = aftaleDatabaseHandler.getById(appointmentId);
            if (aftale == null) {
                LoggerUtility.logError("Kunne ikke finde aftale med ID: " + appointmentId);
                return;
            }

            // Find eller opret kundens ID
            int kundeId = findOrCreateCustomer(customerName);

            // Find medarbejderens ID
            int medarbejderId = findEmployeeIdByName(employee);

            // Find behandlingens ID
            int behandlingId = findTreatmentIdByName(treatment);

            // Hvis alle IDs blev fundet, opdater aftalen
            if (kundeId > 0 && medarbejderId > 0 && behandlingId > 0) {
                aftale.setKundeID(kundeId);
                aftale.setMedarbejderID(medarbejderId);
                aftale.setBehandlingID(behandlingId);
                aftale.setStarttidspunkt(LocalDateTime.of(date, startTime));
                aftale.setSluttidspunkt(LocalDateTime.of(date, endTime));

                // Gem ændringerne i databasen
                aftaleDatabaseHandler.update(aftale);
                LoggerUtility.logEvent("Aftale opdateret med ID: " + appointmentId);
            } else {
                // Log fejl hvis nogle nødvendige data mangler
                LoggerUtility.logError("Kunne ikke opdatere aftale. Manglende kunde, medarbejder eller behandling.");
            }
        } catch (Exception e) {
            // Log generelle fejl under opdateringen
            LoggerUtility.logError("Fejl ved opdatering af aftale: " + e.getMessage());
        }
    }

    // Markerer en aftale som aflyst (sletter ikke fra databasen)
    public void deleteAppointment(int appointmentId) {
        try {
            // Hent aftalen fra databasen
            Aftale aftale = aftaleDatabaseHandler.getById(appointmentId);
            if (aftale == null) {
                LoggerUtility.logError("Kunne ikke finde aftale med ID: " + appointmentId);
                return;
            }

            // Marker aftalen som aflyst
            aftale.setStatus("Aflyst");
            // Gem ændringen i databasen
            aftaleDatabaseHandler.update(aftale);
            LoggerUtility.logEvent("Aftale annulleret med ID: " + appointmentId);
        } catch (Exception e) {
            // Log generelle fejl under aflysningen
            LoggerUtility.logError("Fejl ved annullering af aftale: " + e.getMessage());
        }
    }

    // Henter detaljeret information om en aftale baseret på ID
    public AppointmentData getAppointmentById(int appointmentId) {
        try {
            // Hent aftalen fra databasen
            Aftale aftale = aftaleDatabaseHandler.getById(appointmentId);
            if (aftale == null) {
                return null;
            }

            // Hent relaterede informationer
            Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
            Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());

            // Sæt standardværdi for medarbejderens navn
            String employeeName = "Ukendt";
            // Hent medarbejderinformation hvis muligt
            Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());
            if (medarbejder != null) {
                employeeName = medarbejder.getNavn();
            }

            // Opret og returner et AppointmentData objekt med de hentede informationer
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
            // Log fejl og returner null hvis der opstår problemer
            LoggerUtility.logError("Fejl ved hentning af aftale: " + e.getMessage());
            return null;
        }
    }

    // Konverterer et tidspunkt til en række i kalendervisningen
    public int timeToRow(LocalTime time) {
        // Beregner rækken baseret på time og minut
        // F.eks. 8:00 = række 0, 8:30 = række 1, 9:00 = række 2, osv.
        int hour = time.getHour();
        int minutes = time.getMinute();
        return (hour - 8) * 2 + (minutes >= 30 ? 1 : 0);
    }

    // Henter navne på alle medarbejdere fra databasen
    public List<String> getAllEmployeeNames() {
        List<String> employeeNames = new ArrayList<>();
        try {
            // Hent alle medarbejdere fra databasen
            List<Medarbejder> employees = medarbejderDatabaseHandler.getAll();
            // Tilføj hvert medarbejdernavn til listen
            for (Medarbejder employee : employees) {
                employeeNames.add(employee.getNavn());
            }
        } catch (DatabaseConnectionException e) {
            // Log fejl hvis der opstår problemer
            LoggerUtility.logError("Fejl ved hentning af medarbejdere: " + e.getMessage());
        }
        return employeeNames;
    }

    // Henter navne på alle behandlinger fra databasen
    public List<String> getAllTreatmentNames() {
        List<String> treatmentNames = new ArrayList<>();
        try {
            // Hent alle behandlinger fra databasen
            List<Behandling> treatments = behandlingDatabaseHandler.getAll();
            // Tilføj hver behandlingsnavn til listen
            for (Behandling treatment : treatments) {
                treatmentNames.add(treatment.getBehandling());
            }
        } catch (DatabaseConnectionException e) {
            // Log fejl hvis der opstår problemer
            LoggerUtility.logError("Fejl ved hentning af behandlinger: " + e.getMessage());
        }
        return treatmentNames;
    }

    // Henter navne på alle kunder fra databasen
    public List<String> getAllCustomerNames() {
        List<String> customerNames = new ArrayList<>();
        try {
            // Hent alle kunder fra databasen
            List<Kunde> customers = kundeDatabaseHandler.readAll();
            // Tilføj hvert kundenavn til listen
            for (Kunde customer : customers) {
                customerNames.add(customer.getNavn());
            }
        } catch (DatabaseConnectionException e) {
            // Log fejl hvis der opstår problemer
            LoggerUtility.logError("Fejl ved hentning af kunder: " + e.getMessage());
        }
        return customerNames;
    }

    // Tjekker om et givet tidsrum er ledigt for en medarbejder
    public boolean isTimeSlotAvailable(String employeeName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            // Find medarbejderens ID
            int employeeId = findEmployeeIdByName(employeeName);
            if (employeeId <= 0) {
                return false;
            }

            // Konverter til LocalDateTime-objekter
            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

            // Søg efter overlappende aftaler i databasen
            List<Aftale> conflictingAppointments = aftaleDatabaseHandler.findConflictingAppointments(
                    employeeId, startDateTime, endDateTime);

            // Tidsrummet er ledigt hvis der ikke er konflikter
            return conflictingAppointments.isEmpty();
        } catch (DatabaseConnectionException e) {
            // Log fejl hvis der opstår problemer
            LoggerUtility.logError("Fejl ved kontrol af tidsslot: " + e.getMessage());
            return false;
        }
    }

    // Finder ledige tider for en medarbejder og en specifik behandling
    public List<LocalTime> getAvailableTimeSlots(String employeeName, LocalDate date, String treatmentName) {
        List<LocalTime> availableSlots = new ArrayList<>();
        try {
            // Find medarbejder og behandlings-ID
            int employeeId = findEmployeeIdByName(employeeName);
            int treatmentId = findTreatmentIdByName(treatmentName);

            // Hvis en af dem ikke findes, returner en tom liste
            if (employeeId <= 0 || treatmentId <= 0) {
                return availableSlots;
            }

            // Hent behandlingsinformation for at kende varigheden
            Behandling behandling = behandlingDatabaseHandler.getById(treatmentId);
            if (behandling == null) {
                return availableSlots;
            }

            // Hent varigheden af behandlingen
            int treatmentDuration = behandling.getVarighed();
            // Definer åbningstider
            LocalTime openingTime = LocalTime.of(9, 0);
            LocalTime closingTime = LocalTime.of(18, 0);

            // Tjek hver mulig starttid (med 30 minutters intervaller)
            for (int hour = openingTime.getHour(); hour < closingTime.getHour(); hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalTime startTime = LocalTime.of(hour, minute);
                    LocalTime endTime = startTime.plusMinutes(treatmentDuration);

                    // Hvis sluttiden er efter lukketid, spring over
                    if (endTime.isAfter(closingTime)) {
                        continue;
                    }

                    // Hvis tidsrummet er ledigt, tilføj starttiden til listen
                    if (isTimeSlotAvailable(employeeName, date, startTime, endTime)) {
                        availableSlots.add(startTime);
                    }
                }
            }

        } catch (DatabaseConnectionException e) {
            // Log fejl hvis der opstår problemer
            LoggerUtility.logError("Fejl ved hentning af ledige tider: " + e.getMessage());
        }

        return availableSlots;
    }
}