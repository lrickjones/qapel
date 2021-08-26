package com.qapel.rfid.entities;

import lombok.*;

/**
 * Information to display in station report
 */
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
