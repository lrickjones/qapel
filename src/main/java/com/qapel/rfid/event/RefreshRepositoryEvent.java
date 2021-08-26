package com.qapel.rfid.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event type for refresh repository events to allow interprocess communication
 */
public class RefreshRepositoryEvent extends ApplicationEvent {
    public RefreshRepositoryEvent(Object source) {
        super(source);
    }
}
