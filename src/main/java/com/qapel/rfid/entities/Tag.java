package com.qapel.rfid.entities;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Tag {
    int id;
    String readerName;
    String epc;
    int stationId;
    int antenna;
    String status;
    Timestamp firstRead;
    Timestamp lastRead;
    int numReads;
}
