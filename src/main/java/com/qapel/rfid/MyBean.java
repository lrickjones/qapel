package com.qapel.rfid;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;

@Getter
@Setter
public class MyBean {
    String value;

    public MyBean(String value) {
        this.value = value;
        System.out.println(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
