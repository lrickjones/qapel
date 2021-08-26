package com.qapel.rfid.controller;

import com.qapel.rfid.entities.Station;
import com.qapel.rfid.event.StationChangeEvent;
import com.qapel.rfid.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Station controller for managing and updating station definitions
 * This controller uses the JBA database model using teh station entity
 * This controller uses a blend of Thymeleaf and REST API models, the BaseStationController is the base class
 * with all the shared members and methods
 */
@RequestMapping("/station/")
abstract class BaseStationController {
    // Station repository
    protected final StationRepository stationRepository;

    // Instantiate with station repository
    @Autowired
    public BaseStationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    // Publisher for station change events
    @Autowired
    protected ApplicationEventPublisher stationChange;
}

/**
 * The Thymeleaf controller for station related pages, each function will modify a template and return a reference
 */
@Controller
public class StationHTMLController extends BaseStationController {

    /**
     * Default constructor - called my spring boot controller framework
     * @param stationRepository station repository
     */
    public StationHTMLController(StationRepository stationRepository) {
        super(stationRepository);
    }

    /**
     * Form for adding a new station
     * @param station station to add
     * @return add station page
     */
    @GetMapping("new")
    public String showAddForm(Station station) {
        return "/station/add-station";
    }

    /**
     * List all stations
     * @param model page model
     * @return index page
     */
    @GetMapping("list")
    public String showUpdateForm(Model model) {
        // sort stations by station_order value
        List<Station> stationList = stationRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Station::getStation_order)).collect(Collectors.toList());
        model.addAttribute("stations", stationList);
        return "/station/index";
    }

    /**
     * Add station to the db
     * @param station station to add
     * @param result binding result (system)
     * @param model page model
     * @return index page
     */
    @PostMapping("add")
    public String addStation(@Valid Station station, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/station/add-station";
        }

        stationRepository.save(station);

        // send station change notice to update cached station inf
        stationChange.publishEvent(new StationChangeEvent(this));
        return "redirect:list";
    }

    /**
     * Edit selected station
     * @param id id of station to edit
     * @param model page model
     * @return update station page
     */
    @GetMapping("edit/{id}")
    public String showUpdateForm(@PathVariable("id") int id, Model model) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid station Id: " + id));
        model.addAttribute("station",station);
        return ("/station/update-station");
    }

    /**
     * Update selected station in db
     * @param id id of station to update
     * @param station station information
     * @param result binding result (system)
     * @param model page model
     * @return index page
     */
    @PostMapping("update/{id}")
    public String updateStation(@PathVariable("id") int id, @Valid Station station, BindingResult result, Model model){
        if (result.hasErrors()) {
            station.setId(id);
            return "/station/update-station";
        }
        stationRepository.save(station);

        // send station change notice to update cached station inf
        stationChange.publishEvent(new StationChangeEvent(this));

        model.addAttribute("stations", stationRepository.findAll());
        return "/station/index";
    }

    /**
     * Delete selected station form station table
     * @param id id of station to delete
     * @param model page model
     * @return index page
     */
    @GetMapping("delete/{id}")
    public String deleteStation(@PathVariable("id") int id, Model model) {
        Optional<Station> station = stationRepository.findById(id);
        station.ifPresent(stationRepository::delete);

        // send station change notice to update cached station inf
        stationChange.publishEvent(new StationChangeEvent(this));

        model.addAttribute("stations", stationRepository.findAll());
        return "/station/index";
    }

    /**
     * Select station from a list
     * @param model page model
     * @return select station page
     */
    @GetMapping("select")
    public String selectStation(Model model) {
        model.addAttribute("stations", stationRepository.findAll());
        return "/station/select-station";
    }

}

/**
 * Controller for rest functions that perform actions but do not return pages
 */
@RestController
class StationRestController extends BaseStationController {

    /**
     * Default constructor called by spring boot controller framework
     * @param stationRepository station repository (JBA)
     */
    public StationRestController(StationRepository stationRepository) {
        super(stationRepository);
    }

    /**
     * Find a station by id
     * @param id id of station to find
     * @return Station name or null if no station is found
     */
    @GetMapping("find")
    public String findStation(int id) {
        Station station = stationRepository.findById(id)
                .orElse(null);
        if (station == null) {
            //model.addAttribute("stationName","No Station Selected");
            return "No Station Selected";
        } else {
            //model.addAttribute("stationName", station.getName());
            return station.getName();
        }
    }

}
