package com.qapel.rfid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.ui.dwr.Util;
import org.springframework.stereotype.Service;

@Service
@RemoteProxy
public class ReverseClass {
    private int count = 0;
    /**
     * This method continually calls the update method utill the
     * for loop completes
     */
    @RemoteMethod
    public void callReverseDWR() {
        System.out.println("Ur in callReverseDWR");
        try {
            for (int i = 0; i < 10; i++) {
                update();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Error in callReverseDWR");
            e.printStackTrace();
        }
    }

    /**
     * This method updates ReversePage.jsp &lt;ul id=&quot;updates&quot;&gt;
     * using dwr reverse ajax
     */
    public void update() {
        try {
            List<MyBean> messages = new ArrayList<MyBean>();
            messages.add(new MyBean("testing" + count++));
            Util.addOptions("updates", messages, "value");
        } catch (Exception e) {
            System.out.println("Error in Update");
            e.printStackTrace();
        }
    }
}