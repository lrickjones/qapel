package com.qapel.rfid;

import com.qapel.rfid.entities.QueuedTag;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.ui.dwr.Util;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@RemoteProxy
public class ReverseClass {

    private static final Map<Integer,Queue<QueuedTag>> tagQueue = new ConcurrentHashMap<>();
    /**
     * This method continually calls the update method utill the
     * for loop completes
     */
    @RemoteMethod
    public String callReverseDWR(String station_id) {
        if (tagQueue.isEmpty()) return "";
        try {
            int id = Integer.parseInt(station_id);
            return update(id);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * This method updates ReversePage.jsp <div id="read_tags"></div>"
     * using dwr reverse ajax
     */
    public String update(int id) {
        String result = "";
        Queue<QueuedTag> tag = tagQueue.get(id);
        while (tag != null && !tag.isEmpty()) {
            try {
                QueuedTag t = tag.poll();

                if (t != null) {
                    Util.setValue("read_tags", t.getEpc() + " " + t.getStatus());
                    if (t.getStatus().equalsIgnoreCase("pass")) {
                        Util.setClassName("read_tags", "h1 p-4 m-4 border border-success rounded bg-success");
                        result = "/sound/pass.mp3";
                    } else if (t.getStatus().equalsIgnoreCase("pass")) {
                        Util.setClassName("read_tags", "h1 p-4 m-4 border border-danger rounded bg-danger");
                        result = "/sound/fail.mp3";
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

    public static void enqueue(int id, QueuedTag t) {
        // lookup the id in the list
        Queue<QueuedTag> tag = tagQueue.get(id);
        // if not found create a new queue and add it to the list
        if (tag == null) {
            tag = new LinkedBlockingQueue<>();
            tagQueue.put(id,tag);
        }
        tag.add(t);
    }

}