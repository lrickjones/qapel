package com.qapel.rfid;

import com.qapel.rfid.db.Tag;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.ui.dwr.Util;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@RemoteProxy
public class ReverseClass {

    private static Queue<Tag> tagQueue = new LinkedBlockingQueue<>();
    private static boolean shutdown = false;

    private int count = 0;
    /**
     * This method continually calls the update method utill the
     * for loop completes
     */
    @RemoteMethod
    public void callReverseDWR() {
        //System.out.println("Ur in callReverseDWR");
        try {
            while (!shutdown) {
                update();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Sleep interrupted");
                }
            }
        } catch (Exception e) {
            //System.out.println("Error in callReverseDWR");
            e.printStackTrace();
        }
    }

    /**
     * This method updates ReversePage.jsp &lt;ul id=&quot;updates&quot;&gt;
     * using dwr reverse ajax
     */
    public void update() {
        while (!tagQueue.isEmpty()) {
            try {
                Tag t = tagQueue.poll();
                if (t != null) {
                    //List<MyBean> messages = new ArrayList<>();
                    //messages.add(new MyBean("<div class='h2'>" + t.getEpc() + " read</div>"));
                    Util.setValue("read_tags", "<i>" + t.getEpc() + " read " + count++ + "</i>");
                    if (count%2==0) {
                        Util.setClassName("read_tags", "h1 p4 m4 bg-success");
                    } else {
                        Util.setClassName("read_tags", "h1 p4 m4 bg-danger");
                    }
                    //Util.addOptions("updates", messages, "value");
                }
            } catch (Exception e) {
                System.out.println("Error in Update");
                e.printStackTrace();
            }
        }
    }

    public static void enqueue(Tag t) {
        tagQueue.add(t);
    }

    @PreDestroy
    public void destroy() {
        shutdown = true;
    }
}