package com.example.projektopgave1.Controller;

import com.example.projektopgave1.CustomExceptions.BookingNotPossibleException;
import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.CustomExceptions.HairdresserBusyException;
import com.example.projektopgave1.CustomExceptions.TimeSlotUnavailableException;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import com.example.projektopgave1.Model.Entiteter.Behandling;
import com.example.projektopgave1.Model.Entiteter.Kunde;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import com.example.projektopgave1.Model.UseCases.UseCase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class KalendarOversigtController {

    private final UseCase useCase;

    public KalendarOversigtController() {
        this.useCase = new UseCase();
    }

    public List<Aftale> getAllAppointments() throws DatabaseConnectionException {
        return useCase.getAllAppointments();
    }

    public List<Aftale> getAppointmentsByDate(LocalDate date) throws DatabaseConnectionException {
        return useCase.getAppointmentsByDate(date);
    }

    public List<Aftale> getAppointmentsByEmployee(int medarbejderId) throws DatabaseConnectionException {
        return useCase.getAppointmentsByEmployee(medarbejderId);
    }

    public List<Aftale> getAppointmentsByDateAndEmployee(LocalDate date, int medarbejderId) throws DatabaseConnectionException {
        return useCase.getAppointmentsByDateAndEmployee(date, medarbejderId);
    }

    public Aftale createAppointment(int kundeId, int medarbejderId, int behandlingId, LocalDateTime startDateTime)
            throws DatabaseConnectionException, HairdresserBusyException, TimeSlotUnavailableException, BookingNotPossibleException {
        return useCase.createAppointment(kundeId, medarbejderId, behandlingId, startDateTime);
    }

    public boolean cancelAppointment(int aftaleId) throws DatabaseConnectionException {
        return useCase.cancelAppointment(aftaleId);
    }

    public Aftale moveAppointment(int aftaleId, LocalDateTime newStartDateTime)
            throws DatabaseConnectionException, HairdresserBusyException, TimeSlotUnavailableException, BookingNotPossibleException {
        return useCase.moveAppointment(aftaleId, newStartDateTime);
    }

    public List<LocalTime> getAvailableTimeSlots(LocalDate date, int medarbejderId, int behandlingId)
            throws DatabaseConnectionException {
        return useCase.getAvailableTimeSlots(date, medarbejderId, behandlingId);
    }

    public List<Kunde> getAllCustomers() throws DatabaseConnectionException {
        return useCase.getAllCustomers();
    }

    public List<Medarbejder> getAllEmployees() throws DatabaseConnectionException {
        return useCase.getAllEmployees();
    }

    public List<Behandling> getAllTreatments() throws DatabaseConnectionException {
        return useCase.getAllTreatments();
    }

    public Kunde getCustomerById(int kundeId) throws DatabaseConnectionException {
        return useCase.getCustomerById(kundeId);
    }

    public Medarbejder getEmployeeById(int medarbejderId) throws DatabaseConnectionException {
        return useCase.getEmployeeById(medarbejderId);
    }

    public Behandling getTreatmentById(int behandlingId) throws DatabaseConnectionException {
        return useCase.getTreatmentById(behandlingId);
    }
}