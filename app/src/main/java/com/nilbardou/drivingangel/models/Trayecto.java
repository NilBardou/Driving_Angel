package com.nilbardou.drivingangel.models;

public class Trayecto {

    private String trayecto, cronoTrayecto, cronoDescanso, pulsoMedio, fecha, pulsoMin, pulsoMax, id_trayecto;



    public Trayecto() {

    }

    public Trayecto(String id_trayecto, String trayecto, String cronoTrayecto, String cronoDescanso, String pulsoMedio, String pulsoMin, String pulsoMax, String fecha) {
        this.id_trayecto = id_trayecto;
        this.trayecto = trayecto;
        this.cronoTrayecto = cronoTrayecto;
        this.cronoDescanso = cronoDescanso;
        this.pulsoMedio = pulsoMedio;
        this.pulsoMin = pulsoMin;
        this.pulsoMax = pulsoMax;
        this.fecha = fecha;

    }

    public String getId_trayecto() {
        return id_trayecto;
    }

    public String getTrayecto() {
        return trayecto;
    }

    public String getCronoTrayecto() {
        return cronoTrayecto;
    }

    public String getCronoDescanso() {
        return cronoDescanso;
    }

    public String getPulsoMedio() {
        return pulsoMedio;
    }

    public String getPulsoMin() {
        return pulsoMin;
    }

    public String getPulsoMax() {
        return pulsoMax;
    }

    public String getFecha() {
        return fecha;
    }


    public void setTrayecto(String trayecto) {
        this.trayecto = trayecto;
    }

    public void setCronoTrayecto(String cronoTrayecto) {
        this.cronoTrayecto = cronoTrayecto;
    }

    public void setCronoDescanso(String cronoDescanso) {
        this.cronoDescanso = cronoDescanso;
    }

    public void setPulsoMedio(String pulsoMedio) {
        this.pulsoMedio = pulsoMedio;
    }

    public void setPulsoMin(String pulsoMin) {
        this.pulsoMin = pulsoMin;
    }

    public void setPulsoMax(String pulsoMax) {
        this.pulsoMax = pulsoMax;
    }

    public void setFecha (String fecha){
        this.fecha = fecha;
    }



}
