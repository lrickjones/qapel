package com.qapel.rfid;

import com.qapel.rfid.entities.ReaderConfiguration;
import com.qapel.rfid.entities.Station;
import com.qapel.rfid.repository.ReaderConfigurationRepository;
import com.qapel.rfid.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RequestMapping("/readerConfiguration/")
public class ReaderConfigurationController {
    protected final ReaderConfigurationRepository readerConfigurationRepository;
    protected final StationRepository stationRepository;

    @Autowired
    public ReaderConfigurationController(ReaderConfigurationRepository readerConfigurationRepository,
                                         StationRepository stationRepository) {
        this.readerConfigurationRepository = readerConfigurationRepository;
        this.stationRepository = stationRepository;
    }

    @GetMapping("new")
    public String showAddForm(ReaderConfiguration readerConfiguration, Model model) {
        List<Station> stations = stationRepository.findAll();
        model.addAttribute("stations", stations);
        return "/readerConfiguration/add-readerConfiguration";
    }

    @GetMapping("list")
    public String showUpdateForm(Model model) {
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/index";
    }

    @PostMapping("add")
    public String addReaderConfiguration(@Valid ReaderConfiguration readerConfiguration, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/readerConfiguration/add-readerConfiguration";
        }
        readerConfigurationRepository.save(readerConfiguration);
        return "redirect:list";
    }

    @GetMapping("edit/{id}")
    public String showUpdateForm(@PathVariable("id") int id, Model model) {
        ReaderConfiguration readerConfiguration = readerConfigurationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid readerConfiguration Id: " + id));
        List<Station> stations = stationRepository.findAll();
        model.addAttribute("stations", stations);
        model.addAttribute("readerConfiguration",readerConfiguration);
        return ("/readerConfiguration/update-readerConfiguration");
    }

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
        }
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/index";
    }

    @GetMapping("delete/{id}")
    public String deleteReaderConfiguration(@PathVariable("id") int id, Model model) {
        Optional<ReaderConfiguration> readerConfiguration = readerConfigurationRepository.findById(id);
        readerConfiguration.ifPresent(readerConfigurationRepository::delete);
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/index";
    }

    @GetMapping("select")
    public String selectReaderConfiguration(Model model) {
        model.addAttribute("readerConfigurations", readerConfigurationRepository.findAll());
        return "/readerConfiguration/select-readerConfiguration";
    }

}

