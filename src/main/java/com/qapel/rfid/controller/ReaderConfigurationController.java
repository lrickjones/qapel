package com.qapel.rfid.controller;

import com.qapel.rfid.entities.ReaderConfiguration;
import com.qapel.rfid.entities.Station;
import com.qapel.rfid.event.StationChangeEvent;
import com.qapel.rfid.repository.ReaderConfigurationRepository;
import com.qapel.rfid.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller for handling reader configuration (stations table in db)
 * This controller uses the JBA model for data base access using ReaderConfiguration and Station entities
 * This controller is set up as a Thymeleaf template, each call populates a template and returns the template
 * for display. Some calls perform additional db actions
 */
@Controller
@RequestMapping("/readerConfiguration/")
public class ReaderConfigurationController {
    // Station change event publisher
    @Autowired
    private ApplicationEventPublisher stationChange;

    // JBA connections
    protected final ReaderConfigurationRepository readerConfigurationRepository;
    protected final StationRepository stationRepository;

    // Instantiate object with JBA repositories
    @Autowired
    public ReaderConfigurationController(ReaderConfigurationRepository readerConfigurationRepository,
                                         StationRepository stationRepository) {
        this.readerConfigurationRepository = readerConfigurationRepository;
        this.stationRepository = stationRepository;
    }

    /**
     * Display page for creating a new reader configuration
     * @param readerConfiguration reader configuration to add
     * @param model page model
     * @return add-readerConfiguration page
     */
    @GetMapping("new")
    public String showAddForm(ReaderConfiguration readerConfiguration, Model model) {
        List<Station> stations = stationRepository.findAll();
        model.addAttribute("stations", stations);
        return "/readerConfiguration/add-readerConfiguration";
    }

    /**
     * HTML page to show all reader configurations
     * @param model page model
     * @return index page
     */
    @GetMapping("list")
    public String showUpdateForm(Model model) {
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/index";
    }

    /**
     * Add a new reader coniguration to the stations table
     * @param readerConfiguration reader configuration
     * @param result binding result
     * @param model page model
     * @return index page
     */
    @PostMapping("add")
    public String addReaderConfiguration(@Valid ReaderConfiguration readerConfiguration, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/readerConfiguration/add-readerConfiguration";
        }
        readerConfigurationRepository.save(readerConfiguration);

        // send station change notice to update cached station inf
        stationChange.publishEvent(new StationChangeEvent(this));
        return "redirect:list";
    }

    /**
     * Edit selected reader configuration
     * @param id id of record to edit
     * @param model page model
     * @return update configuration page
     */
    @GetMapping("edit/{id}")
    public String showUpdateForm(@PathVariable("id") int id, Model model) {
        ReaderConfiguration readerConfiguration = readerConfigurationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid readerConfiguration Id: " + id));
        List<Station> stations = stationRepository.findAll();
        model.addAttribute("stations", stations);
        model.addAttribute("readerConfiguration",readerConfiguration);
        return ("/readerConfiguration/update-readerConfiguration");
    }

    /**
     * Update record in stations db
     * @param id id of record to update
     * @param readerConfiguration reader configuration values to use
     * @param result binding result
     * @param model page model
     * @return index page
     */
    @PostMapping("update/{id}")
    public String updateReaderConfiguration(@PathVariable("id") int id, @Valid ReaderConfiguration readerConfiguration, BindingResult result, Model model){
        if (result.hasErrors()) {
            readerConfiguration.setId(id);
            return "/readerConfiguration/update-readerConfiguration";
        }
        Optional<Station> station = stationRepository.findById(readerConfiguration.getStation_id());
        if (station.isPresent()) {
            readerConfiguration.setStation(station.get());
            readerConfigurationRepository.save(readerConfiguration);
            // send station change notice to update cached station inf
            stationChange.publishEvent(new StationChangeEvent(this));
        }
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/index";
    }

    /**
     * Remove selected id from db
     * @param id id of record to remove
     * @param model page model
     * @return index page
     */
    @GetMapping("delete/{id}")
    public String deleteReaderConfiguration(@PathVariable("id") int id, Model model) {
        Optional<ReaderConfiguration> readerConfiguration = readerConfigurationRepository.findById(id);
        readerConfiguration.ifPresent(readerConfigurationRepository::delete);

        // send station change notice to update cached station inf
        stationChange.publishEvent(new StationChangeEvent(this));
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/index";
    }

    /**
     * Select a record from list
     * @param model page model
     * @return select page
     */
    @GetMapping("select")
    public String selectReaderConfiguration(Model model) {
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/select-readerConfiguration";
    }

}

