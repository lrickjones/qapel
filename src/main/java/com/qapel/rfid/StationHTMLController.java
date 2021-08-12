package com.qapel.rfid;

import com.qapel.rfid.entities.Station;
import com.qapel.rfid.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/station/")
abstract class BaseStationController {
    protected final StationRepository stationRepository;

    @Autowired
    public BaseStationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }
}


@Controller
public class StationHTMLController extends BaseStationController {


    public StationHTMLController(StationRepository stationRepository) {
        super(stationRepository);
    }

    @GetMapping("new")
    public String showAddForm(Station station) {
        return "/station/add-station";
    }

    @GetMapping("list")
    public String showUpdateForm(Model model) {
        // sort stations by station_order value
        List<Station> stationList = stationRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Station::getStation_order)).collect(Collectors.toList());
        model.addAttribute("stations", stationList);
        return "/station/index";
    }

    @PostMapping("add")
    public String addStation(@Valid Station station, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/station/add-station";
        }

        stationRepository.save(station);
        return "redirect:list";
    }

    @GetMapping("edit/{id}")
    public String showUpdateForm(@PathVariable("id") int id, Model model) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid station Id: " + id));
        model.addAttribute("station",station);
        return ("/station/update-station");
    }

    @PostMapping("update/{id}")
    public String updateStation(@PathVariable("id") int id, @Valid Station station, BindingResult result, Model model){
        if (result.hasErrors()) {
            station.setId(id);
            return "/station/update-station";
        }
        stationRepository.save(station);
        model.addAttribute("stations", stationRepository.findAll());
        return "/station/index";
    }

    @GetMapping("delete/{id}")
    public String deleteStation(@PathVariable("id") int id, Model model) {
        Optional<Station> station = stationRepository.findById(id);
        station.ifPresent(stationRepository::delete);
        model.addAttribute("stations", stationRepository.findAll());
        return "/station/index";
    }

    @GetMapping("select")
    public String selectStation(Model model) {
        model.addAttribute("stations", stationRepository.findAll());
        return "/station/select-station";
    }

}

@RestController
class StationRestController extends BaseStationController {

    public StationRestController(StationRepository stationRepository) {
        super(stationRepository);
    }

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
