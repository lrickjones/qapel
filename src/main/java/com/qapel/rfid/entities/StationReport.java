package com.qapel.rfid.entities;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StationReport {
    private String stationName;
    private String epc;
    private String status;
}
