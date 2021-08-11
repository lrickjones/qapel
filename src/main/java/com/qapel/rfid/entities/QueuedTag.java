package com.qapel.rfid.entities;

import lombok.*;

/**
 * Use this to capture information to be used for tags queued up for polling
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QueuedTag {
    private int stationId;
    private String epc;
    private String status;
}
