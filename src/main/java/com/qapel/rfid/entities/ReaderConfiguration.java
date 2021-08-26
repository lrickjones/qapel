package com.qapel.rfid.entities;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * Stations entity setup as a JBA entity for managing reader configuration in stations table
 */
@Entity
@Table(name="stations")
public class ReaderConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotBlank(message = "Station id is mandatory")
    @Column(name="station_id")
    private int station_id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="station_id",insertable = false,updatable = false)
    private Station station;

    @NotBlank(message = "Reader name is mandatory")
    @Column(name="reader_name")
    private String reader_name;

    @NotBlank(message = "Antenna is mandatory")
    @Column(name = "antenna")
    private int antenna;

    @NotBlank(message = "Status is mandatory")
    @Column(name = "status")
    private String status;

    public ReaderConfiguration(){}

    public ReaderConfiguration(int station_id, String reader_name, int antenna, String status) {
        this.station_id = station_id;
        this.reader_name = reader_name;
        this.antenna = antenna;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStation_id() {
        return station_id;
    }

    public void setStation_id(int station_id) {
        this.station_id = station_id;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Station getStation(){
        return station;
    }

    public String getReader_name() {
        return reader_name;
    }

    public void setReader_name(String reader_name) {
        this.reader_name = reader_name;
    }

    public int getAntenna() {
        return antenna;
    }

    public void setAntenna(int antenna) {
        this.antenna = antenna;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
