package com.example.projektopgave1.Model.Entiteter;

public class Medarbejder {
    private int medarbejderID;
    private String navn;
    private String brugernavn;
    private String adgangskode;
    private String nummer;
    private String mail;

    public Medarbejder() {}

    public Medarbejder(String navn, String brugernavn, String adgangskode, String nummer, String mail) {
        this.navn = navn;
        this.brugernavn = brugernavn;
        this.adgangskode = adgangskode;
        this.nummer = nummer;
        this.mail = mail;
    }

    public int getMedarbejderID() {
        return medarbejderID;
    }

    public void setMedarbejderID(int medarbejderID) {
        this.medarbejderID = medarbejderID;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getBrugernavn() {
        return brugernavn;
    }

    public void setBrugernavn(String brugernavn) {
        this.brugernavn = brugernavn;
    }

    public String getAdgangskode() {
        return adgangskode;
    }

    public void setAdgangskode(String adgangskode) {
        this.adgangskode = adgangskode;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}