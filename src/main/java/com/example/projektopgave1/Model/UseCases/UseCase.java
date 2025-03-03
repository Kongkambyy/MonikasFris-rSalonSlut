package com.example.projektopgave1.Model.UseCases;

import com.example.projektopgave1.CustomExceptions.BookingNotPossibleException;
import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.CustomExceptions.HairdresserBusyException;
import com.example.projektopgave1.CustomExceptions.InvalidInputException;
import com.example.projektopgave1.CustomExceptions.TimeSlotUnavailableException;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import com.example.projektopgave1.Model.Entiteter.Behandling;
import com.example.projektopgave1.Model.Entiteter.Kunde;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import com.example.projektopgave1.Model.DatabaseHandlers.AftaleDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.BehandlingDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.KundeDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.MedarbejderDatabaseHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UseCase {
    private final AftaleDatabaseHandler aftaleDatabaseHandler;
    private final KundeDatabaseHandler kundeDatabaseHandler;
    private final MedarbejderDatabaseHandler medarbejderDatabaseHandler;
    private final BehandlingDatabaseHandler behandlingDatabaseHandler;

    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$|^[0-9]{2}\\s[0-9]{2}\\s[0-9]{2}\\s[0-9]{2}$");

    public UseCase() {
        this.aftaleDatabaseHandler = new AftaleDatabaseHandler();
        this.kundeDatabaseHandler = new KundeDatabaseHandler();
        this.medarbejderDatabaseHandler = new MedarbejderDatabaseHandler();
        this.behandlingDatabaseHandler = new BehandlingDatabaseHandler();
    }

    public Aftale createAppointment(int kundeId, int medarbejderId, int behandlingId, LocalDateTime startDateTime)
            throws DatabaseConnectionException, HairdresserBusyException, TimeSlotUnavailableException, BookingNotPossibleException {
        if (startDateTime.toLocalTime().isBefore(OPENING_TIME) || startDateTime.toLocalTime().isAfter(CLOSING_TIME)) {
            throw new TimeSlotUnavailableException("Tid kan ikke bookes uden for åbningstiden (9:00-18:00)",
                    startDateTime, startDateTime);
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new BookingNotPossibleException("Kan ikke oprette booking i fortiden");
        }
        Behandling behandling = behandlingDatabaseHandler.getById(behandlingId);
        if (behandling == null) {
            throw new BookingNotPossibleException("Behandlingen eksisterer ikke");
        }
        LocalDateTime endDateTime = startDateTime.plusMinutes(behandling.getVarighed());
        if (endDateTime.toLocalTime().isAfter(CLOSING_TIME)) {
            throw new TimeSlotUnavailableException("Behandlingen vil slutte efter lukketid",
                    startDateTime, endDateTime);
        }
        if (!isEmployeeAvailable(medarbejderId, startDateTime, endDateTime)) {
            throw new HairdresserBusyException("Frisøren er optaget i det valgte tidsrum",
                    medarbejderId, startDateTime.toString());
        }
        Aftale aftale = new Aftale();
        aftale.setKundeID(kundeId);
        aftale.setMedarbejderID(medarbejderId);
        aftale.setBehandlingID(behandlingId);
        aftale.setStarttidspunkt(startDateTime);
        aftale.setSluttidspunkt(endDateTime);
        aftale.setStatus("Booket");
        aftale.setOprettelsesdato(LocalDateTime.now());
        return aftaleDatabaseHandler.create(aftale);
    }

    public List<Aftale> getAllAppointments() throws DatabaseConnectionException {
        return aftaleDatabaseHandler.getAll();
    }

    public List<Aftale> getAppointmentsByDate(LocalDate date) throws DatabaseConnectionException {
        return aftaleDatabaseHandler.findByDate(date);
    }

    public List<Aftale> getAppointmentsByEmployee(int medarbejderId) throws DatabaseConnectionException {
        return aftaleDatabaseHandler.findByEmployee(medarbejderId);
    }

    public List<Aftale> getAppointmentsByDateAndEmployee(LocalDate date, int medarbejderId) throws DatabaseConnectionException {
        List<Aftale> employeeAppointments = aftaleDatabaseHandler.findByEmployee(medarbejderId);
        List<Aftale> result = new ArrayList<>();
        for (Aftale appointment : employeeAppointments) {
            if (appointment.getStarttidspunkt().toLocalDate().equals(date)) {
                result.add(appointment);
            }
        }
        return result;
    }

    public List<Aftale> getCustomerAppointments(int kundeId) throws DatabaseConnectionException {
        return aftaleDatabaseHandler.findByCustomer(kundeId);
    }

    public boolean cancelAppointment(int aftaleId) throws DatabaseConnectionException {
        Aftale aftale = aftaleDatabaseHandler.getById(aftaleId);
        if (aftale == null) {
            return false;
        }
        aftale.setStatus("Aflyst");
        Aftale updatedAftale = aftaleDatabaseHandler.update(aftale);
        return updatedAftale != null;
    }

    public Aftale moveAppointment(int aftaleId, LocalDateTime newStartDateTime)
            throws DatabaseConnectionException, HairdresserBusyException, TimeSlotUnavailableException, BookingNotPossibleException {
        Aftale aftale = aftaleDatabaseHandler.getById(aftaleId);
        if (aftale == null) {
            throw new BookingNotPossibleException("Aftalen eksisterer ikke");
        }
        if (newStartDateTime.toLocalTime().isBefore(OPENING_TIME) || newStartDateTime.toLocalTime().isAfter(CLOSING_TIME)) {
            throw new TimeSlotUnavailableException("Tid kan ikke bookes uden for åbningstiden (9:00-18:00)",
                    newStartDateTime, newStartDateTime);
        }
        if (newStartDateTime.isBefore(LocalDateTime.now())) {
            throw new BookingNotPossibleException("Kan ikke flytte booking til en tid i fortiden");
        }
        Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());
        LocalDateTime newEndDateTime = newStartDateTime.plusMinutes(behandling.getVarighed());
        if (newEndDateTime.toLocalTime().isAfter(CLOSING_TIME)) {
            throw new TimeSlotUnavailableException("Behandlingen vil slutte efter lukketid",
                    newStartDateTime, newEndDateTime);
        }
        if (!isEmployeeAvailableExcluding(aftale.getMedarbejderID(), newStartDateTime, newEndDateTime, aftaleId)) {
            throw new HairdresserBusyException("Frisøren er optaget i det valgte tidsrum",
                    aftale.getMedarbejderID(), newStartDateTime.toString());
        }
        aftale.setStarttidspunkt(newStartDateTime);
        aftale.setSluttidspunkt(newEndDateTime);
        return aftaleDatabaseHandler.update(aftale);
    }

    public boolean isEmployeeAvailable(int medarbejderId, LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws DatabaseConnectionException {
        List<Aftale> employeeAppointments = aftaleDatabaseHandler.findByEmployee(medarbejderId);
        return isEmployeeAvailable(medarbejderId, startDateTime, endDateTime, employeeAppointments);
    }

    public boolean isEmployeeAvailable(int medarbejderId, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                       List<Aftale> employeeAppointments) {
        for (Aftale appointment : employeeAppointments) {
            if ("Aflyst".equals(appointment.getStatus())) {
                continue;
            }
            if (!(endDateTime.isBefore(appointment.getStarttidspunkt()) ||
                    startDateTime.isAfter(appointment.getSluttidspunkt()))) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmployeeAvailableExcluding(int medarbejderId, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                int excludeAftaleId) throws DatabaseConnectionException {
        List<Aftale> employeeAppointments = aftaleDatabaseHandler.findByEmployee(medarbejderId);
        List<Aftale> filteredAppointments = new ArrayList<>();
        for (Aftale appointment : employeeAppointments) {
            if (appointment.getAftaleID() != excludeAftaleId) {
                filteredAppointments.add(appointment);
            }
        }
        return isEmployeeAvailable(medarbejderId, startDateTime, endDateTime, filteredAppointments);
    }

    public List<LocalTime> getAvailableTimeSlots(LocalDate date, int medarbejderId, int behandlingId)
            throws DatabaseConnectionException {
        Behandling behandling = behandlingDatabaseHandler.getById(behandlingId);
        if (behandling == null) {
            return new ArrayList<>();
        }
        int treatmentDuration = behandling.getVarighed();
        List<Aftale> employeeAppointments = aftaleDatabaseHandler.findByEmployee(medarbejderId);
        List<Aftale> dateAppointments = new ArrayList<>();
        for (Aftale appointment : employeeAppointments) {
            if (appointment.getStarttidspunkt().toLocalDate().equals(date)) {
                dateAppointments.add(appointment);
            }
        }
        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime currentTime = OPENING_TIME;
        while (currentTime.plusMinutes(treatmentDuration).isBefore(CLOSING_TIME) ||
                currentTime.plusMinutes(treatmentDuration).equals(CLOSING_TIME)) {
            allPossibleSlots.add(currentTime);
            currentTime = currentTime.plusMinutes(30);
        }
        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime startTime : allPossibleSlots) {
            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(treatmentDuration);
            if (isEmployeeAvailable(medarbejderId, startDateTime, endDateTime, dateAppointments)) {
                availableSlots.add(startTime);
            }
        }
        return availableSlots;
    }

    public Kunde createCustomer(String navn, String nummer, String mail, String adresse)
            throws InvalidInputException, DatabaseConnectionException {
        validateCustomerData(navn, nummer, mail);
        Kunde kunde = new Kunde(0, navn, nummer, mail, adresse);
        return kundeDatabaseHandler.create(kunde);
    }

    public Kunde getCustomerById(int kundeId) throws DatabaseConnectionException {
        return kundeDatabaseHandler.readById(kundeId);
    }

    public List<Kunde> getAllCustomers() throws DatabaseConnectionException {
        return kundeDatabaseHandler.readAll();
    }

    public boolean updateCustomer(int kundeId, String navn, String nummer, String mail, String adresse)
            throws InvalidInputException, DatabaseConnectionException {
        validateCustomerData(navn, nummer, mail);
        Kunde kunde = new Kunde(kundeId, navn, nummer, mail, adresse);
        return kundeDatabaseHandler.update(kunde);
    }

    public boolean deleteCustomer(int kundeId) throws DatabaseConnectionException {
        return kundeDatabaseHandler.delete(kundeId);
    }

    public List<Kunde> searchCustomersByName(String searchTerm) throws DatabaseConnectionException {
        List<Kunde> allCustomers = kundeDatabaseHandler.readAll();
        List<Kunde> result = new ArrayList<>();
        for (Kunde kunde : allCustomers) {
            if (kunde.getNavn().toLowerCase().contains(searchTerm.toLowerCase())) {
                result.add(kunde);
            }
        }
        return result;
    }

    private void validateCustomerData(String navn, String nummer, String mail) throws InvalidInputException {
        if (navn == null || navn.trim().isEmpty()) {
            throw new InvalidInputException("Navn må ikke være tomt", "Navn");
        }
        if (nummer != null && !nummer.trim().isEmpty() && !PHONE_PATTERN.matcher(nummer).matches()) {
            throw new InvalidInputException("Ugyldigt telefonnummer format", "Nummer", nummer, "8 cifre, eventuelt adskilt af mellemrum (f.eks. 12 34 56 78)");
        }
        if (mail != null && !mail.trim().isEmpty() && !EMAIL_PATTERN.matcher(mail).matches()) {
            throw new InvalidInputException("Ugyldig email format", "Mail", mail, "example@domain.com");
        }
    }

    public Behandling createTreatment(String navn, int varighed, int pris)
            throws InvalidInputException, DatabaseConnectionException {
        validateTreatmentData(navn, varighed, pris);
        Behandling behandling = new Behandling();
        behandling.setBehandling(navn);
        behandling.setVarighed(varighed);
        behandling.setPris(pris);
        return behandlingDatabaseHandler.create(behandling);
    }

    public Behandling getTreatmentById(int behandlingId) throws DatabaseConnectionException {
        return behandlingDatabaseHandler.getById(behandlingId);
    }

    public List<Behandling> getAllTreatments() throws DatabaseConnectionException {
        return behandlingDatabaseHandler.getAll();
    }

    public boolean updateTreatment(int behandlingId, String navn, int varighed, int pris)
            throws InvalidInputException, DatabaseConnectionException {
        validateTreatmentData(navn, varighed, pris);
        Behandling behandling = new Behandling();
        behandling.setBehandlingID(behandlingId);
        behandling.setBehandling(navn);
        behandling.setVarighed(varighed);
        behandling.setPris(pris);
        return behandlingDatabaseHandler.update(behandling);
    }

    public boolean deleteTreatment(int behandlingId) throws DatabaseConnectionException {
        return behandlingDatabaseHandler.delete(behandlingId);
    }

    public List<Behandling> getTreatmentsByDurationRange(int minDuration, int maxDuration) throws DatabaseConnectionException {
        List<Behandling> allTreatments = behandlingDatabaseHandler.getAll();
        List<Behandling> result = new ArrayList<>();
        for (Behandling behandling : allTreatments) {
            if (behandling.getVarighed() >= minDuration && behandling.getVarighed() <= maxDuration) {
                result.add(behandling);
            }
        }
        return result;
    }

    public List<Behandling> getTreatmentsByPriceRange(int minPrice, int maxPrice) throws DatabaseConnectionException {
        List<Behandling> allTreatments = behandlingDatabaseHandler.getAll();
        List<Behandling> result = new ArrayList<>();
        for (Behandling behandling : allTreatments) {
            if (behandling.getPris() >= minPrice && behandling.getPris() <= maxPrice) {
                result.add(behandling);
            }
        }
        return result;
    }

    private void validateTreatmentData(String navn, int varighed, int pris) throws InvalidInputException {
        if (navn == null || navn.trim().isEmpty()) {
            throw new InvalidInputException("Behandlingsnavn må ikke være tomt", "Behandling");
        }
        if (varighed <= 0) {
            throw new InvalidInputException("Varighed skal være større end 0", "Varighed", String.valueOf(varighed));
        }
        if (pris < 0) {
            throw new InvalidInputException("Pris kan ikke være negativ", "Pris", String.valueOf(pris));
        }
    }

    public Medarbejder createEmployee(String navn, String brugernavn, String adgangskode, String nummer, String mail)
            throws InvalidInputException, DatabaseConnectionException {
        validateEmployeeData(navn, brugernavn, adgangskode, nummer, mail);
        if (!isUsernameAvailable(brugernavn)) {
            throw new InvalidInputException("Brugernavn er allerede i brug", "Brugernavn", brugernavn);
        }
        Medarbejder medarbejder = new Medarbejder(navn, brugernavn, adgangskode, nummer, mail);
        return medarbejderDatabaseHandler.create(medarbejder);
    }

    public Medarbejder getEmployeeById(int medarbejderId) throws DatabaseConnectionException {
        return medarbejderDatabaseHandler.getById(medarbejderId);
    }

    public List<Medarbejder> getAllEmployees() throws DatabaseConnectionException {
        return medarbejderDatabaseHandler.getAll();
    }

    public boolean updateEmployee(int medarbejderId, String navn, String brugernavn, String adgangskode,
                                  String nummer, String mail)
            throws InvalidInputException, DatabaseConnectionException {
        validateEmployeeData(navn, brugernavn, adgangskode, nummer, mail);
        Medarbejder existingEmployee = medarbejderDatabaseHandler.getById(medarbejderId);
        if (existingEmployee != null && !existingEmployee.getBrugernavn().equals(brugernavn)) {
            if (!isUsernameAvailable(brugernavn)) {
                throw new InvalidInputException("Brugernavn er allerede i brug", "Brugernavn", brugernavn);
            }
        }
        Medarbejder medarbejder = new Medarbejder(navn, brugernavn, adgangskode, nummer, mail);
        medarbejder.setMedarbejderID(medarbejderId);
        return medarbejderDatabaseHandler.update(medarbejder);
    }

    public boolean deleteEmployee(int medarbejderId) throws DatabaseConnectionException {
        return medarbejderDatabaseHandler.delete(medarbejderId);
    }

    public boolean isUsernameAvailable(String brugernavn) throws DatabaseConnectionException {
        return medarbejderDatabaseHandler.isUsernameAvailable(brugernavn);
    }

    public Medarbejder authenticateEmployee(String brugernavn, String adgangskode) throws DatabaseConnectionException {
        List<Medarbejder> employees = medarbejderDatabaseHandler.getAll();
        for (Medarbejder medarbejder : employees) {
            if (medarbejder.getBrugernavn().equals(brugernavn) &&
                    medarbejder.getAdgangskode().equals(adgangskode)) {
                return medarbejder;
            }
        }
        return null;
    }

    public List<Aftale> getActiveAppointmentsInRange(LocalDate startDate, LocalDate endDate)
            throws DatabaseConnectionException {
        List<Aftale> allAppointments = aftaleDatabaseHandler.getActiveAppointmentsInRange(startDate, endDate);
        return allAppointments.stream()
                .filter(a -> !a.getStatus().equals("Aflyst"))
                .collect(Collectors.toList());
    }

    public List<Aftale> getFilteredAppointments(LocalDate date, List<Integer> employeeIds, String statusFilter)
            throws DatabaseConnectionException {
        List<Aftale> appointments = aftaleDatabaseHandler.findByDate(date);
        List<Aftale> filteredAppointments = new ArrayList<>();

        for (Aftale appointment : appointments) {
            // Filter by employee
            boolean includeByEmployee = employeeIds.isEmpty() || employeeIds.contains(appointment.getMedarbejderID());

            // Filter by status
            boolean includeByStatus = true;
            if ("ACTIVE".equals(statusFilter) && "Aflyst".equals(appointment.getStatus())) {
                includeByStatus = false;
            }

            if (includeByEmployee && includeByStatus) {
                filteredAppointments.add(appointment);
            }
        }

        return filteredAppointments;
    }

    public List<Integer> getSelectedEmployeeIds(boolean allSelected, List<Integer> selectedIds) {
        if (allSelected) {
            // Return empty list to indicate no filtering
            return new ArrayList<>();
        }
        return new ArrayList<>(selectedIds);
    }

    public List<LocalTime> getAvailableTimesForDay(LocalDate date, int medarbejderId, int behandlingId, int excludeAftaleId)
            throws DatabaseConnectionException {

        // Get the treatment to determine its duration
        Behandling behandling = behandlingDatabaseHandler.getById(behandlingId);
        if (behandling == null) {
            return new ArrayList<>();
        }

        int treatmentDuration = behandling.getVarighed();

        // Get all appointments for the employee on this date
        List<Aftale> employeeAppointments = aftaleDatabaseHandler.findByEmployee(medarbejderId);
        List<Aftale> dateAppointments = new ArrayList<>();

        for (Aftale appointment : employeeAppointments) {
            if (appointment.getStarttidspunkt().toLocalDate().equals(date) &&
                    appointment.getAftaleID() != excludeAftaleId &&
                    !appointment.getStatus().equals("Aflyst")) {
                dateAppointments.add(appointment);
            }
        }

        // Generate all possible time slots from opening to closing time
        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime currentTime = OPENING_TIME;
        while (currentTime.plusMinutes(treatmentDuration).isBefore(CLOSING_TIME) ||
                currentTime.plusMinutes(treatmentDuration).equals(CLOSING_TIME)) {
            allPossibleSlots.add(currentTime);
            currentTime = currentTime.plusMinutes(30);
        }

        // Filter out unavailable time slots
        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime startTime : allPossibleSlots) {
            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(treatmentDuration);

            // Check if time is in the past
            if (startDateTime.isBefore(LocalDateTime.now())) {
                continue;
            }

            // Check if slot is available
            boolean isAvailable = true;
            for (Aftale appointment : dateAppointments) {
                LocalDateTime appointmentStart = appointment.getStarttidspunkt();
                LocalDateTime appointmentEnd = appointment.getSluttidspunkt();

                // Check for overlap
                if (!(endDateTime.isBefore(appointmentStart) || startDateTime.isAfter(appointmentEnd))) {
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable) {
                availableSlots.add(startTime);
            }
        }

        return availableSlots;
    }

    public List<Medarbejder> searchEmployeesByName(String searchTerm) throws DatabaseConnectionException {
        return medarbejderDatabaseHandler.findByName(searchTerm);
    }

    private void validateEmployeeData(String navn, String brugernavn, String adgangskode,
                                      String nummer, String mail) throws InvalidInputException {
        if (navn == null || navn.trim().isEmpty()) {
            throw new InvalidInputException("Navn må ikke være tomt", "Navn");
        }
        if (brugernavn == null || brugernavn.trim().isEmpty()) {
            throw new InvalidInputException("Brugernavn må ikke være tomt", "Brugernavn");
        }
        if (adgangskode == null || adgangskode.trim().isEmpty()) {
            throw new InvalidInputException("Adgangskode må ikke være tom", "Adgangskode");
        }
        if (nummer != null && !nummer.trim().isEmpty() && !PHONE_PATTERN.matcher(nummer).matches()) {
            throw new InvalidInputException("Ugyldigt telefonnummer format", "Nummer", nummer, "8 cifre, eventuelt adskilt af mellemrum (f.eks. 12 34 56 78)");
        }
        if (mail != null && !mail.trim().isEmpty() && !EMAIL_PATTERN.matcher(mail).matches()) {
            throw new InvalidInputException("Ugyldig email format", "Mail", mail, "example@domain.com");
        }
    }
}
