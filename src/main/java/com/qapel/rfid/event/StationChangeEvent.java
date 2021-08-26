package com.qapel.rfid.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event type for station change events for interprocess communication
 */
public class StationChangeEvent extends ApplicationEvent {
    public StationChangeEvent(Object source) {
        super(source);
    }
}
