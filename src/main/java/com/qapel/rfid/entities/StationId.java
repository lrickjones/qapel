package com.qapel.rfid.entities;

import lombok.*;

/**
 * Station Id entity used for generating unique ids for looking up station in cache
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StationId {
    private String readerName;
    private int antenna;
    private String status;

    /**
     * Create a cache index from reader name and antenna
     * @param name reader name
     * @param antenna antenna
     * @return formatted index for storing and retrieving requests in cache
     */
    public static String indexFromReader(String name, int antenna) {
        return name + ":" + antenna;
    }
}
