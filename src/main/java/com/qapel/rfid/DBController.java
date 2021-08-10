package com.qapel.rfid;

import com.qapel.rfid.db.Station;
import com.qapel.rfid.db.Tag;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class DBController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    String insert_tag = "INSERT INTO tags (Reader_Name, EPC, Antenna, First_Read, Last_Read, Num_Reads) VALUES (?,?,?,?,?,?)";
    String view = "SELECT * FROM reader.tags";
    String lookup_station = "SELECT * FROM reader.station";

    @PostMapping("/test")
    public String test() throws Exception {
        int result = jdbcTemplate.update(insert_tag, "test1", "0000-0000", 1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 4);
        ReverseClass.enqueue(new Tag("test1","00002341234",1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 4));
        System.out.println(result + " rows added.");
        return "add_tag";
    }

    @PostMapping("/add_tag")
    public String addTag(@RequestBody String request) {

        JSONObject jsonHeader = new JSONObject(request);
        JSONArray jsonArray = jsonHeader.getJSONArray("tag_reads");
        String readerName = jsonHeader.getString("reader_name");

        for (int j=0; j<jsonArray.length(); j++) {
            JSONObject json = jsonArray.getJSONObject(j);
            if (!json.getBoolean("isHeartBeat")) {
                String edc = json.getString("epc");
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

                int result = jdbcTemplate.update(insert_tag, readerName, edc, antenna, firstRead, lastRead, readNum);
                ReverseClass.enqueue(new Tag(readerName, edc,antenna, firstRead, lastRead, readNum));
            }
        }
        return "add_tag";
    }
    @GetMapping("/home/homeSignedIn")
    public String allTags(@RequestParam(required = false) String id, Model model) {

        List<Tag> tagList = new ArrayList();
        jdbcTemplate.query(view, (ResultSetExtractor<Object>) rs -> {
            while (rs.next()) {
                Tag tag  = Tag.builder().readerName(rs.getString("Reader_Name")).epc(rs.getString("EPC"))
                        .antenna(rs.getInt("Antenna")).lastRead(rs.getTimestamp("Last_Read"))
                        .firstRead(rs.getTimestamp("First_Read")).numReads(rs.getInt("Num_Reads")).build();
                tagList.add(tag);
            }
            return tagList;
        });
        model.addAttribute("tagList", tagList);
        return "/home/homeSignedIn";
    }
}
