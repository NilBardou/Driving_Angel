package com.nilbardou.drivingangel;

import android.app.Application;

public class VarGlobal extends Application {

    private static long tStart = 0, tStart2 = 0, tBuff = 0, tBuff2 = 0;
    private static Boolean run = false, conectado = false;
    private static int sec = 0, hour = 0, min = 0, sec_desc = 0, hour_desc = 0, min_desc = 0, pulsaciones = 00, maximo_pulsaciones = 0, minimo_pulsaciones = 0,
            media_pulsaciones = 0, contador_pulsaciones = 0, cont = 1;


    public long gettStart() {
        return tStart;
    }

    public void settStart(long tStart) {
        this.tStart = tStart;
    }

    public long gettStart2() {
        return tStart2;
    }

    public void settStart2(long tStart2) {
        this.tStart2 = tStart2;
    }

    public long gettBuff() {
        return tBuff;
    }

    public void settBuff(long tBuff) {
        this.tBuff = tBuff;
    }

    public long gettBuff2() {
        return tBuff2;
    }

    public void settBuff2(long tBuff2) {
        this.tBuff2 = tBuff2;
    }

    public Boolean getRun() {
        return run;
    }

    public void setRun(Boolean run) {
        this.run = run;
    }

    public Boolean getConectado() {
        return conectado;
    }

    public void setConectado(Boolean conectado) {
        this.conectado = conectado;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getSec_desc() {
        return sec_desc;
    }

    public void setSec_desc(int sec_desc) {
        this.sec_desc = sec_desc;
    }

    public int getHour_desc() {
        return hour_desc;
    }

    public void setHour_desc(int hour_desc) {
        this.hour_desc = hour_desc;
    }

    public int getMin_desc() {
        return min_desc;
    }

    public void setMin_desc(int min_desc) {
        this.min_desc = min_desc;
    }

    public int getPulso() {
        return pulsaciones;
    }

    public void setPulso(int pulsaciones) {
        this.pulsaciones = pulsaciones;
    }

    public int getMaximo() {
        return maximo_pulsaciones;
    }

    public void setMaximo(int maximo_pulsaciones) {
        this.maximo_pulsaciones = maximo_pulsaciones;
    }

    public int getMinimo() {
        return minimo_pulsaciones;
    }

    public void setMinimo(int minimo_pulsaciones) {
        this.minimo_pulsaciones = minimo_pulsaciones;
    }

    public int getMedia() {
        return media_pulsaciones;
    }

    public void setMedia(int media_pulsaciones) {
        this.media_pulsaciones = media_pulsaciones;
    }

    public int getContador() {
        return contador_pulsaciones;
    }

    public void setContador(int contador_pulsaciones) {

        this.contador_pulsaciones = contador_pulsaciones;
    }

    public int getCont() {
        return cont;
    }

    public void setCont(int cont) {
        this.cont = cont;
    }
}
