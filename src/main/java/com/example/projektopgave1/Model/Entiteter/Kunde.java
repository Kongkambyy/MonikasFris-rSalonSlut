package com.example.projektopgave1.Model.Entiteter;

public class Kunde {

    private int KundeID;
    private String Navn;
    private String Nummer;
    private String Mail;
    private String Adresse;

    public Kunde(int KundeID, String Navn, String Nummer, String Mail, String Adresse) {
        this.KundeID = KundeID;
        this.Navn = Navn;
        this.Nummer = Nummer;
        this.Mail = Mail;
        this.Adresse = Adresse;
    }

    public int getKundeID() {
        return KundeID;
    }

    public void setKundeID(int KundeID) {
        this.KundeID = KundeID;
    }

    public String getNavn() {
        return Navn;
    }

    public void setNavn(String Navn) {
        this.Navn = Navn;
    }

    public String getNummer() {
        return Nummer;
    }

    public void setNummer(String nummer) {
        this.Nummer = nummer;
    }

    public String getMail() {
        return Mail;
    }

    public void setMail(String Mail) {
        this.Mail = Mail;
    }

    public String getAdresse() {
        return Adresse;
    }

    public void setAdresse(String Adresse) {
        this.Adresse = Adresse;
    }
}