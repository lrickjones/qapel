package com.qapel.rfid.entities;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StationId {
    private String readerName;
    private int antenna;
    private String status;

    public static String indexFromReader(String name, int antenna) {
        return name + ":" + antenna;
    }
}
