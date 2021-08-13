package com.qapel.rfid.entities;

import lombok.*;

/**
 * Cache station information that doesn't often change, so it can be looked up from information on tag without
 * querying the database
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StationCache {
    private int stationId;
    private String status;
}
