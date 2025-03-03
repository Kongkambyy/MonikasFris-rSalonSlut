package com.example.projektopgave1.Model.Entiteter;

public class Behandling {

    private int BehandlingID;
    private String Behandling;
    private int Varighed;
    private int Pris;

    public Behandling() {}

    public Behandling(int BehandlinID, String Behandling, int Varighed, int Pris) {
        this.BehandlingID = BehandlinID;
        this.Behandling = Behandling;
        this.Varighed = Varighed;
        this.Pris = Pris;
    }

    public int getBehandlingID() {
        return BehandlingID;
    }

    public void setBehandlingID(int BehandlingID) {
        this.BehandlingID = BehandlingID;
    }

    public String getBehandling() {
        return Behandling;
    }

    public void setBehandling(String Behandling) {
        this.Behandling = Behandling;
    }

    public int getVarighed() {
        return Varighed;
    }

    public void setVarighed(int Varighed) {
        this.Varighed = Varighed;
    }

    public int getPris() {
        return Pris;
    }

    public void setPris(int Pris) {
        this.Pris = Pris;
    }
}
