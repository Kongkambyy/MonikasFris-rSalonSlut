package com.example.projektopgave1.Model.Entiteter;

import java.time.LocalDateTime;

public class Aftale {

    private int AftaleID;
    private int KundeID;
    private int MedarbejderID;
    private int BehandlingID;
    private LocalDateTime Starttidspunkt;
    private LocalDateTime Sluttidspunkt;
    private String Status;
    private LocalDateTime Oprettelsesdato;

    private Kunde kunde;
    private Medarbejder medarbejder;
    private Behandling behandling;

    public Aftale() {}

    public Aftale(int AftaleID, int KundeID, int MedarbejderID, LocalDateTime Starttidspunkt,
                  LocalDateTime Sluttidspunkt, String Status, LocalDateTime oprettelsesdato) {

        this.AftaleID = AftaleID;
        this.KundeID = KundeID;
        this.MedarbejderID = MedarbejderID;
        this.Starttidspunkt = Starttidspunkt;
        this.Sluttidspunkt = Sluttidspunkt;
        this.Status = Status;
        this.Oprettelsesdato = oprettelsesdato;
    }

    public int getAftaleID() {
        return AftaleID;
    }

    public void setAftaleID(int AftaleID) {
        this.AftaleID = AftaleID;
    }

    public int getKundeID() {
        return KundeID;
    }

    public void setKundeID(int KundeID) {
        this.KundeID = KundeID;
    }

    public int getMedarbejderID() {
        return MedarbejderID;
    }

    public void setMedarbejderID(int MedarbejderID) {
        this.MedarbejderID = MedarbejderID;
    }

    public int getBehandlingID() {
        return BehandlingID;
    }

    public void setBehandlingID(int BehandlingID) {
        this.BehandlingID = BehandlingID;
    }

    public LocalDateTime getStarttidspunkt() {
        return Starttidspunkt;
    }

    public void setStarttidspunkt(LocalDateTime Starttidspunkt) {
        this.Starttidspunkt = Starttidspunkt;
    }

    public LocalDateTime getSluttidspunkt() {
        return Sluttidspunkt;
    }

    public void setSluttidspunkt(LocalDateTime Sluttidspunkt) {
        this.Sluttidspunkt = Sluttidspunkt;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public LocalDateTime getOprettelsesdato() {
        return Oprettelsesdato;
    }

    public void setOprettelsesdato(LocalDateTime Oprettelsesdato) {
        this.Oprettelsesdato = Oprettelsesdato;
    }

    public Kunde getKunde() {
        return kunde;
    }

    public void setKunde(Kunde kunde) {
        this.kunde = kunde;
    }

    public Medarbejder getMedarbejder() {
        return medarbejder;
    }

    public void setMedarbejder(Medarbejder medarbejder) {
        this.medarbejder = medarbejder;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }
}