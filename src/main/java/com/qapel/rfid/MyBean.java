package com.qapel.rfid;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyBean {
    String value;

    public MyBean(String value) {
        this.value = value;
        System.out.println(value);
    }
}
