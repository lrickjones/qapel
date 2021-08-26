package com.qapel.rfid.controller;

import com.qapel.rfid.entities.StationReport;
import com.qapel.rfid.entities.Tag;
import com.qapel.rfid.event.RefreshRepositoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Base controller for managing repository to support additional classes for thymeleaf and rest apis
 * This uses the JdbcTemplate model for accessing the database
 */
@RequestMapping("/repository")
abstract class TagRepositoryBaseController implements ApplicationListener<RefreshRepositoryEvent> {
    // error logger
    protected static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    // spring boot will set up jdbcTemplate based on parameters in application.properties
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    // query templates
    private static final String getAll = "SELECT * FROM reader.tags LIMIT 20;";
    private static final String deleteId = "DELETE FROM reader.tags WHERE (id = ?)";
    private static final String getFromRepository = "SElECT * FROM reader.repository WHERE epc=? AND station_id=?";
    private static final String insertTag = "INSERT INTO reader.repository " +
            "(epc, station_id, final_status, first_read, last_read, num_reads) " +
            "VALUES (?,?,?,?,?,?)";
    private static final String updateTag = "UPDATE reader.repository SET station_id=?, final_status=?," +
            "first_read=?, last_read=?, num_reads=? WHERE epc=? AND station_id=?";
    protected static final String stationReport = "SELECT station.name, repository.epc, repository.final_status FROM " +
            "reader.repository INNER JOIN station ON repository.station_id=station.id";

    /**
     * Refresh the repository by reading the tags table and adding new tags to repository and updating existing tags
     */
    public void refresh() {
        boolean stuffToDo = true;
        while (stuffToDo) {
            List<Tag> tagList = new ArrayList<>();
            jdbcTemplate.query(getAll, (ResultSetExtractor<Object>) rs -> {
                while (rs.next()) {
                    Tag tag = Tag.builder().id(rs.getInt("id"))
                            .readerName(rs.getString("reader_name"))
                            .antenna(rs.getInt("antenna")).epc(rs.getString("epc"))
                            .stationId(rs.getInt("station_id"))
                            .status(rs.getString("status"))
                            .firstRead(rs.getTimestamp("first_read"))
                            .lastRead(rs.getTimestamp("last_read"))
                            .numReads(rs.getInt("num_reads"))
                            .build();
                    tagList.add(tag);
                }
                return tagList;
            });
            if (tagList.isEmpty()) {
                stuffToDo = false;
            } else {
                for (Tag tag: tagList) {
                    if (moveTagToRepository(tag)) {
                        jdbcTemplate.update(deleteId, tag.getId());
                    } else {
                        logger.error("Unable to move tag to repository - " + tag.getEpc() + ":" + tag.getReaderName()
                        + ":" + tag.getAntenna() + " - Tag not removed from tags!");
                        stuffToDo = false;
                    }
                }
            }
        }
    }

    /**
     * Move a tag from tag table to repository
     * @param tag tag to be moved
     * @return true if tag is successfully moved, otherwise false
     */
    private boolean moveTagToRepository(Tag tag) {
        Tag qTag = jdbcTemplate.query(getFromRepository, rs-> {
            if (rs.next()) {
                return Tag.builder().epc(rs.getString("epc")).stationId(rs.getInt("station_id"))
                        .firstRead(rs.getTimestamp("first_read")).lastRead(rs.getTimestamp("last_read"))
                        .status(rs.getString("final_status")).numReads(rs.getInt("num_reads")).build();
            } else {
                return null;
            }
        }, tag.getEpc(), tag.getStationId());
        if (qTag == null) {
            //epc, station_id, final_status, first_read, last_read, num_reads
            try {
                jdbcTemplate.update(insertTag, tag.getEpc(), tag.getStationId(), tag.getStatus(), tag.getFirstRead(),
                        tag.getLastRead(), tag.getNumReads());
            } catch (DataAccessException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Timestamp firstRead = tag.getFirstRead().before(qTag.getFirstRead())?tag.getFirstRead():qTag.getFirstRead();
            Timestamp lastRead = tag.getLastRead().after(qTag.getLastRead())?tag.getLastRead():qTag.getLastRead();
            int numReads = tag.getNumReads() + qTag.getNumReads();
            // get status shouldn't be null, but if so we will process it without changing the status
            String status = (tag.getStatus() != null && tag.getStatus().equalsIgnoreCase("fail"))?tag.getStatus():qTag.getStatus();
            try {
                jdbcTemplate.update(updateTag, qTag.getStationId(), status, firstRead, lastRead,
                        numReads, qTag.getEpc(), qTag.getStationId());
            } catch (DataAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true; // don't update
    }

    /**
     * Listen for repository refresh events from controllers inside this sprint boot environment
     * @param repositoryEvent RefreshRepositoryEvent
     */
    @Override
    public void onApplicationEvent(RefreshRepositoryEvent repositoryEvent) {
        this.refresh();
    }

}

/**
 * Thymeleaf controller for repository reports
 */
@Controller
class TagRepositoryHTMLController extends TagRepositoryBaseController {

    /**
     * List all the records in the repository
     * @param response htmlservlettresponse (system)
     * @param model page model
     * @return station report page
     */
    @GetMapping("/station_report")
    public String stationReport(HttpServletResponse response,
                          Model model) {
        List<StationReport> stationReportList = new ArrayList<>();
        jdbcTemplate.query(stationReport, (ResultSetExtractor<Object>) rs -> {
            while (rs.next()) {
                StationReport stationReport = StationReport.builder().stationName(rs.getString("station.name"))
                        .epc(rs.getString("repository.epc")).status(rs.getString("repository.final_status")).build();
                stationReportList.add(stationReport);
            }
            return stationReportList;
        });
        model.addAttribute("stationReportList", stationReportList);

        return "/repository/station_report";
    }
}

/**
 * Rest API calls for requests with no thymeleaf template
 */
@RestController
class TagRepositoryRestController extends TagRepositoryBaseController {
    /**
     * Rest API to refresh repository from external processes
     */
    @GetMapping("/refresh")
    public void doRefresh() {
        this.refresh();
    }

}
