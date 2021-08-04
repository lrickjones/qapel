package com.qapel.rfid.db;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Tag {
    String readerName;
    String epc;
    int antenna;
    Timestamp firstRead;
    Timestamp lastRead;
    int numReads;
}
