package com.qapel.rfid.controller;

import com.qapel.rfid.entities.QueuedTag;
import com.qapel.rfid.entities.StationId;
import com.qapel.rfid.entities.Tag;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manage tag related api calls and thymeleaf templates
 * Base controller allows sharing between the two different controllers, REST and Thymeleaf
 */
@RequestMapping("/tag")
abstract class BaseTagController {
    protected static final Map<String,Integer> stationIdMapper = new ConcurrentHashMap<>();
    protected static final Map<Integer, Queue<QueuedTag>> tagQueue = new ConcurrentHashMap<>();
    static final String insertTag = "INSERT INTO tags (Reader_Name, EPC, Antenna, First_Read, Last_Read, Num_Reads) " +
            "VALUES (?,?,?,?,?,?)";
    //static final String view = "SELECT * FROM reader.tags";
    static final String readerName2StationId = "SELECT DISTINCT reader_name, station_id, antenna from reader.stations";
    static final String getStations = "SELECT DISTINCT reader_name, antenna, status from reader.stations WHERE station_id = ?";
    static final String lookup_status = "SELECT DISTINCT status from reader.stations WHERE station_id=? AND reader_name=? AND antenna=?";
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void updateStationIdMapper() {
        stationIdMapper.clear();
        jdbcTemplate.query(readerName2StationId, (ResultSetExtractor<Object>) rs -> {
            while (rs.next()) {
                int station_id = rs.getInt("station_id");
                // store combination of reader name and antenna to map to station so a reader can
                // support more than one station using different antenna assignments
                String reader_name = StationId.indexFromReader(rs.getString("reader_name"), rs.getInt("antenna"));
                stationIdMapper.put(reader_name, station_id);
            }
            return stationIdMapper;
        });
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

/**
 * Restful interface for tags
 */
@RestController
class TagRestController extends BaseTagController {

    /**
     * This method continually calls the update method utill the
     * for loop completes
     */
    @GetMapping("/poll")
    public String poll(@RequestParam String station_id) {
        if (tagQueue.isEmpty()) return null;
        try {
            int id = Integer.parseInt(station_id);
            return update(id);
        } catch (NumberFormatException e) {
            return null;
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
                    result = String.format("{\"epc\":\"%s\", \"status\":\"%s\"}", t.getEpc(), t.getStatus());
                    /*
                        Util.setClassName("read_tags", "h1 p-4 m-4 border border-success rounded bg-success");
                        result = "/sound/pass.mp3";
                    */
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * For testing purposes during development
     * @param id Station id
     * @return Tag generated in test request
     */
    @PostMapping("/test")
    public Tag test(@RequestParam int id, @RequestParam String status) {
        Tag tag =  new Tag("test1", "00002341234", 1, new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()), 4);
        jdbcTemplate.update(insertTag, tag.getReaderName(), tag.getEpc(), tag.getAntenna(),
                tag.getFirstRead(),tag.getLastRead(),tag.getNumReads());
        enqueue(id, QueuedTag.builder().stationId(id).epc(tag.getEpc()).status(status).build());
        return tag;
    }

    @GetMapping("/update_stations")
    public void updateStations() {
        this.updateStationIdMapper();
    }

    public String getStatus(int stationId, String readerName, int antenna) {
        AtomicReference<String> passed = new AtomicReference<>();
        jdbcTemplate.query(lookup_status,(ResultSetExtractor<Object>) rs -> {
            while (rs.next()) {
                passed.set(rs.getString("status"));
            }
            return passed;
        },stationId, readerName, antenna);
        return passed.get();
    }

    /**
     * Add tag from JSON sent by impinj reader connect
     * @param request JSON with tag information from impinj reader
     * @return tag added
     */
    @PostMapping("/impinj/add_tag")
    public Tag addTag(@RequestBody String request) {

        JSONObject jsonHeader = new JSONObject(request);
        JSONArray jsonArray = jsonHeader.getJSONArray("tag_reads");
        String readerName = jsonHeader.getString("reader_name");
        Tag tag = null;

        for (int j = 0; j < jsonArray.length(); j++) {
            JSONObject json = jsonArray.getJSONObject(j);
            if (!json.getBoolean("isHeartBeat")) {
                String epc = json.getString("epc");
                int antenna = json.getInt("antennaPort");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
                long elapsed;
                try {
                    Date parsedDate = dateFormat.parse(json.getString("firstSeenTimestamp"));
                    elapsed = parsedDate.getTime();
                } catch (Exception e) {
                    elapsed = System.currentTimeMillis();
                }
                Timestamp firstRead = new Timestamp(elapsed);
                Timestamp lastRead = new Timestamp(elapsed);
                int readNum = 0;
                jdbcTemplate.update(insertTag, readerName, epc, antenna, firstRead, lastRead, readNum);
                Integer station_id = stationIdMapper.get(StationId.indexFromReader(readerName, antenna));
                if (station_id != null) {
                    tag = new Tag(readerName, epc, antenna, firstRead, lastRead, readNum);
                    enqueue(station_id, QueuedTag.builder().stationId(station_id).epc(epc).status(getStatus(station_id, readerName, antenna)).build());
                } else {
                    System.out.println("No station ID registered to show tag: "
                            + StationId.indexFromReader(readerName, antenna));
                }
            }
        }
        return tag;
    }
}
/**
 * Thymeleaf api for tag related pages
 */
@Controller
class TagHTMLController extends BaseTagController {
    /**
     * Page for monitoring tags added for station assigned to this page
     * @param id Station ID (if null this page is not active and needs to be initialized with a station)
     * @param model Model used to update page
     * @return Reference to /tag/monitor.html template
     */
    @GetMapping("/monitor")
    public String monitor(@RequestParam(required = false) String id,
                          @CookieValue(value = "station_id", required = false) String idCookie,
                          HttpServletResponse response,
                          Model model) {
        //TODO: This maps on startup only, need to add refresh when station updates
        if (stationIdMapper.isEmpty()) {
            this.updateStationIdMapper();
        }
        // If there is no station id as parameter or web page, force id selection
        if (id == null && idCookie == null) {
            return "redirect:/station/select";
        } else if (id != null) {
            // if id != null set the cookie and use it as the id
            response.addCookie(new Cookie("station_id", id));
        }
        // id has precedence over idCookie
        String useId = (id == null?idCookie:id);

        List<StationId> stationList = new ArrayList<>();
        jdbcTemplate.query(getStations, (ResultSetExtractor<Object>) rs -> {
            while (rs.next()) {
                StationId stationId = StationId.builder().readerName(rs.getString("reader_name"))
                        .antenna(rs.getInt("antenna")).status(rs.getString("status")).build();
                stationList.add(stationId);
            }
            return stationList;
        }, useId);
        model.addAttribute("stationList", stationList);

        return "/tag/monitor";
    }

}

