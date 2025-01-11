package ro.faur.apollo.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.domain.dto.HomeDTO;
import ro.faur.apollo.home.service.HomeService;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @PostMapping()
    public ResponseEntity<HomeDTO> createHome(@RequestParam String name, @RequestParam String address) {
        return ResponseEntity.ok(homeService.createHome(name, address));
    }

    @GetMapping
    public ResponseEntity<List<HomeDTO>> getHomesForCurrentUser() {
        return ResponseEntity.ok(homeService.getHomesForCurrentUser());
    }

    @GetMapping("/{homeUuid}")
    public ResponseEntity<HomeDTO> getHome(@PathVariable String homeUuid) {
        HomeDTO home = homeService.getHome(homeUuid);
        return ResponseEntity.ok(home);
    }

}
