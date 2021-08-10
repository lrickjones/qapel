package com.qapel.rfid;

import com.qapel.rfid.db.Tag;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.ui.dwr.Util;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
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
    public String callReverseDWR() {
        if (tagQueue.isEmpty()) return "";
        return update();
    }

    /**
     * This method updates ReversePage.jsp &lt;ul id=&quot;updates&quot;&gt;
     * using dwr reverse ajax
     */
    public String update() {
        String result = "";
        while (!tagQueue.isEmpty()) {
            try {
                Tag t = tagQueue.poll();
                if (t != null) {
                    Util.setValue("read_tags", t.getEpc() + " read ");
                    if (++count%2==0) {
                        Util.setClassName("read_tags", "h1 p-4 m-4 border border-success rounded bg-success");
                        result = "/sound/pass.mp3";
                    } else {
                        Util.setClassName("read_tags", "h1 p-4 m-4 border border-danger rounded bg-danger");
                        result = "/sound/fail.mp3";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void enqueue(Tag t) {
        tagQueue.add(t);
    }

    @PreDestroy
    public void destroy() {
        shutdown = true;
    }
}