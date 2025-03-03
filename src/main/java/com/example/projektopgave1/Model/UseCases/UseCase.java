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

        return employeeAppointments.stream()
                .filter(aftale -> aftale.getStarttidspunkt().toLocalDate().equals(date))
                .collect(Collectors.toList());
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
            if (appointment.getStatus().equals("Aflyst")) {
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

        List<Aftale> filteredAppointments = employeeAppointments.stream()
                .filter(aftale -> aftale.getAftaleID() != excludeAftaleId)
                .collect(Collectors.toList());

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
        List<Aftale> dateAppointments = employeeAppointments.stream()
                .filter(aftale -> aftale.getStarttidspunkt().toLocalDate().equals(date))
                .collect(Collectors.toList());

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime currentTime = OPENING_TIME;

        while (currentTime.plusMinutes(treatmentDuration).isBefore(CLOSING_TIME) ||
                currentTime.plusMinutes(treatmentDuration).equals(CLOSING_TIME)) {
            allPossibleSlots.add(currentTime);
            currentTime = currentTime.plusMinutes(30);
        }

        return allPossibleSlots.stream()
                .filter(startTime -> {
                    LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(treatmentDuration);

                    return isEmployeeAvailable(medarbejderId, startDateTime, endDateTime, dateAppointments);
                })
                .collect(Collectors.toList());
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

        return allCustomers.stream()
                .filter(kunde -> kunde.getNavn().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }

    private void validateCustomerData(String navn, String nummer, String mail) throws InvalidInputException {
        if (navn == null || navn.trim().isEmpty()) {
            throw new InvalidInputException("Navn må ikke være tomt", "Navn");
        }

        if (nummer != null && !nummer.trim().isEmpty() && !PHONE_PATTERN.matcher(nummer).matches()) {
            throw new InvalidInputException(
                    "Ugyldigt telefonnummer format",
                    "Nummer",
                    nummer,
                    "8 cifre, eventuelt adskilt af mellemrum (f.eks. 12 34 56 78)"
            );
        }

        if (mail != null && !mail.trim().isEmpty() && !EMAIL_PATTERN.matcher(mail).matches()) {
            throw new InvalidInputException(
                    "Ugyldig email format",
                    "Mail",
                    mail,
                    "example@domain.com"
            );
        }
    }

    public Behandling createTreatment(String navn, int varighed, int pris)
            throws InvalidInputException, DatabaseConnectionException {

        validateTreatmentData(navn, varighed, pris);

        // Use default constructor and set values instead of the 3-argument constructor
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

        // Use default constructor and set values
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

        return allTreatments.stream()
                .filter(behandling -> behandling.getVarighed() >= minDuration && behandling.getVarighed() <= maxDuration)
                .collect(Collectors.toList());
    }

    public List<Behandling> getTreatmentsByPriceRange(int minPrice, int maxPrice) throws DatabaseConnectionException {
        List<Behandling> allTreatments = behandlingDatabaseHandler.getAll();

        return allTreatments.stream()
                .filter(behandling -> behandling.getPris() >= minPrice && behandling.getPris() <= maxPrice)
                .collect(Collectors.toList());
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

        return employees.stream()
                .filter(medarbejder -> medarbejder.getBrugernavn().equals(brugernavn) &&
                        medarbejder.getAdgangskode().equals(adgangskode))
                .findFirst()
                .orElse(null);
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
            throw new InvalidInputException(
                    "Ugyldigt telefonnummer format",
                    "Nummer",
                    nummer,
                    "8 cifre, eventuelt adskilt af mellemrum (f.eks. 12 34 56 78)"
            );
        }

        if (mail != null && !mail.trim().isEmpty() && !EMAIL_PATTERN.matcher(mail).matches()) {
            throw new InvalidInputException(
                    "Ugyldig email format",
                    "Mail",
                    mail,
                    "example@domain.com"
            );
        }
    }
}