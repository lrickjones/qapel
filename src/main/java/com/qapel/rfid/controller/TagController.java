package com.qapel.rfid.controller;

import com.qapel.rfid.entities.QueuedTag;
import com.qapel.rfid.entities.StationCache;
import com.qapel.rfid.entities.StationId;
import com.qapel.rfid.entities.Tag;
import com.qapel.rfid.event.RefreshRepositoryEvent;
import com.qapel.rfid.event.StationChangeEvent;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manage tag related api calls and thymeleaf templates
 * Base controller allows sharing between the two different controllers, REST and Thymeleaf
 */
@RequestMapping("/tag")
abstract class BaseTagController implements ApplicationListener<StationChangeEvent> {
    protected static final Logger logger = LoggerFactory.getLogger(ErrorController.class);
    protected static ConcurrentMap<String, StationCache> stationIdMapper = new ConcurrentHashMap<>();
    protected static final ConcurrentMap<Integer, Queue<QueuedTag>> tagQueue = new ConcurrentHashMap<>();
    static final String insertTag = "INSERT INTO tags " +
            "(Reader_Name, EPC, Antenna, Status, Station_Id, First_Read, Last_Read, Num_Reads) " +
            "VALUES (?,?,?,?,?,?,?,?)";
    //static final String view = "SELECT * FROM reader.tags";
    static final String readerName2StationId = "SELECT DISTINCT reader_name, station_id, antenna from reader.stations";
    static final String getStations = "SELECT DISTINCT reader_name, antenna, status from reader.stations WHERE station_id = ?";
    static final String lookup_status = "SELECT DISTINCT status from reader.stations WHERE station_id=? AND reader_name=? AND antenna=?";
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void updateStationIdMapper() {
        ConcurrentMap<String, StationCache> newIdMapper = new ConcurrentHashMap<>();
        jdbcTemplate.query(readerName2StationId, (ResultSetExtractor<Object>) rs -> {
            while (rs.next()) {
                String readerName = rs.getString("reader_name");
                int antenna = rs.getInt("antenna");
                int station_id = rs.getInt("station_id");
                //TODO: May want to optimize this as part of a join query with reader configuration table
                String status = getStatus(station_id, readerName, antenna);
                // store combination of reader name and antenna to map to station so a reader can
                // support more than one station using different antenna assignments
                String readerIdx = StationId.indexFromReader(readerName, antenna);
                newIdMapper.put(readerIdx, new StationCache(station_id, status));
            }
            return newIdMapper;
        });
        stationIdMapper = newIdMapper;
    }

    /**
     * Event hook to allow controllers in same environment to update the stationId mapper
     * External controllers and processes need to use the REST API in the TagRestController
      */
    @Override
    public void onApplicationEvent(StationChangeEvent event) {
        this.updateStationIdMapper();
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
}

/**
 * Restful interface for tags
 */
@RestController
class TagRestController extends BaseTagController {
     @Autowired
     private ApplicationEventPublisher repositoryRefresh;

    /**
     * If there are any events in the queue call check queue to find a matching event,
     * return empty string if queue is empty
     */
    @GetMapping("/poll")
    public String poll(@RequestParam String station_id) {
        if (tagQueue.isEmpty()) return null;
        try {
            int id = Integer.parseInt(station_id);
            return checkQueue(id);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * Check queue for incoming event, return json string with event info if found, otherwise return empty string
     */
    public String checkQueue(int id) {
        String result = "";
        Queue<QueuedTag> tag = tagQueue.get(id);
        while (tag != null && !tag.isEmpty()) {
            try {
                QueuedTag t = tag.poll();
                if (t != null) {
                    result = String.format("{\"epc\":\"%s\", \"status\":\"%s\"}", t.getEpc(), t.getStatus());
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
        Tag tag =  Tag.builder().readerName("Test Reader")
                .epc("00002341234")
                .antenna(1)
                .status(status)
                .firstRead(new Timestamp(System.currentTimeMillis()))
                .lastRead(new Timestamp(System.currentTimeMillis()))
                .numReads(4).build();
        jdbcTemplate.update(insertTag, tag.getReaderName(), tag.getEpc(), tag.getAntenna(), tag.getStatus(),
                tag.getStationId(), tag.getFirstRead(),tag.getLastRead(),tag.getNumReads());
        enqueue(id, QueuedTag.builder().stationId(id).epc(tag.getEpc()).status(status).build());
        return tag;
    }

    /**
     * Update the station lookup cache, this should be called any time the station or stations table are updated
     */
    @GetMapping("/update_stations")
    public void updateStations() {
        this.updateStationIdMapper();
    }

    private static final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();

    /**
     * Parse iso8601 formatted date into timestamp
     * @param date ISO8601 formatted date
     * @return Timestamp or now() if there is an error parsing the date
     */
    private Timestamp parseIsoDate(String date) {
        long elapsed;
        try {
            DateTime parsedDate = dateFormatter.parseDateTime(date);
            elapsed = parsedDate.getMillis();
        } catch (Exception e) {
            elapsed = System.currentTimeMillis();
        }
        return new Timestamp(elapsed);
    }

    /**
     * Add tag from JSON sent by impinj reader connect to database and queue up for feedback to client
     * @param request JSON with tag information from impinj reader
     */
    @PostMapping("/impinj/add_tag")
    public void addTag(@RequestBody String request) {

        // Get meta information from tag read
        JSONObject jsonHeader = new JSONObject(request);
        String readerName = jsonHeader.getString("reader_name");        // reader is in meta
        JSONArray jsonArray = jsonHeader.getJSONArray("tag_reads");     // list of tags from reader

        Map<String, Tag> tagMap = new HashMap<>();
        for (int j = 0; j < jsonArray.length(); j++) {
            // read tag j from json Array
            JSONObject json = jsonArray.getJSONObject(j);
            // Ignore heartbeat tags TODO: make use of heartbeat to test connection
            if (!json.getBoolean("isHeartBeat")) {
                // read tag fields
                String epc = json.getString("epc");
                int antenna = json.getInt("antennaPort");

                // should set Speedway Connect Output:Timestamp Format to ISO8601, if not try Unix Time as a fail-safe
                Timestamp timestamp;
                try {
                    timestamp = parseIsoDate(json.getString("firstSeenTimestamp"));
                } catch(JSONException e) {
                    timestamp = new Timestamp(json.getLong("firstSeenTimeStamp") * 1000L);
                }

                // look for tag in map to see if it is duplicated
                Tag tag = tagMap.get(epc + ":" + antenna);
                if (tag == null) {
                    // if tag is new, add it to the collection
                    tag = Tag.builder().readerName(readerName).epc(epc).antenna(antenna)
                            .firstRead(timestamp).lastRead(timestamp).numReads(1).build();
                    tagMap.put(epc + ":" + antenna, tag);
                } else {
                    // if the tag exists, update the tag
                    if (timestamp.before(tag.getFirstRead())) {
                        tag.setFirstRead(timestamp);
                    }
                    if (timestamp.after(tag.getLastRead())) {
                        tag.setLastRead(timestamp);
                    }
                    tag.setNumReads(tag.getNumReads() + 1);
                }
            }
        }
        // for each unique tag found
        for (Tag tag: tagMap.values()) {
            StationCache stationInfo = stationIdMapper.get(StationId.indexFromReader(readerName, tag.getAntenna()));
            if (stationInfo == null) {
                // if station info not found, make sure cache is up to date
                updateStationIdMapper();
                stationInfo = stationIdMapper.get(StationId.indexFromReader(readerName, tag.getAntenna()));
                logger.error("Station not found in station id cache: " + readerName + ": " + tag.getAntenna());
            }
            // add tag to database
            try {
                // Get default info for stationId and status so read can be recorded
                String status = stationInfo == null?null:stationInfo.getStatus();
                int stationId = stationInfo == null?0:stationInfo.getStationId();
                jdbcTemplate.update(insertTag, tag.getReaderName(), tag.getEpc(), tag.getAntenna(), status,
                        stationId, tag.getFirstRead(), tag.getLastRead(), tag.getNumReads());
                repositoryRefresh.publishEvent(new RefreshRepositoryEvent(this));
            } catch (DataAccessException e) {
                e.printStackTrace();
            }
            // queue up tag for processing on page
            if (stationInfo != null) {
                enqueue(stationInfo.getStationId(),
                        QueuedTag.builder()
                                .stationId(stationInfo.getStationId())
                                .epc(tag.getEpc())
                                .status(stationInfo.getStatus()).build());
            } else {
                logger.error("No station ID registered to show tag: "
                        + StationId.indexFromReader(readerName, tag.getAntenna()));
            }
        }
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

