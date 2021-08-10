package com.qapel.rfid.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name="station")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotBlank(message = "Name is mandatory")
    @Column(name="name")
    private String name;

    @NotBlank(message = "Station order is mandatory")
    @Column(name = "station_order")
    private int station_order;

    public Station() {}

    public Station(String name, int station_order) {
        this.name = name;
        this.station_order = station_order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStation_order() {
        return station_order;
    }

    public void setStation_order(int station_order) {
        this.station_order = station_order;
    }
}
