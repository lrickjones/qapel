package com.qapel.rfid.event;

import org.springframework.context.ApplicationEvent;

public class RefreshRepositoryEvent extends ApplicationEvent {
    public RefreshRepositoryEvent(Object source) {
        super(source);
    }
}
