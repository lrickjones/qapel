package com.qapel.rfid;

import com.qapel.rfid.db.Station;
import com.qapel.rfid.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/station/")
abstract class BaseStationController {
    protected final StationRepository stationRepository;

    @Autowired
    public BaseStationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }
}


@Controller

public class StationController extends BaseStationController {


    public StationController(StationRepository stationRepository) {
        super(stationRepository);
    }

    @GetMapping("new")
    public String showAddForm(Station station) {
        return "/station/add-station";
    }

    @GetMapping("list")
    public String showUpdateForm(Model model) {
        model.addAttribute("stations", stationRepository.findAll());
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
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id: " + id));
        stationRepository.delete(station);
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
class RestStationController extends BaseStationController {

    public RestStationController(StationRepository stationRepository) {
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
