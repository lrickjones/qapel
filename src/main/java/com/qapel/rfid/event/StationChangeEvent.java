package com.qapel.rfid.event;

import org.springframework.context.ApplicationEvent;

public class StationChangeEvent extends ApplicationEvent {
    public StationChangeEvent(Object source) {
        super(source);
    }
}
