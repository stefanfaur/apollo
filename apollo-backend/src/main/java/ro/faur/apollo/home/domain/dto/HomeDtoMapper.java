package ro.faur.apollo.home.domain.dto;

import org.springframework.stereotype.Component;
import ro.faur.apollo.device.domain.dtos.DeviceDtoMapper;
import ro.faur.apollo.home.domain.Home;

import java.util.List;

@Component
public class HomeDtoMapper {

    private final DeviceDtoMapper deviceDtoMapper;

    public HomeDtoMapper(DeviceDtoMapper deviceDtoMapper) {
        this.deviceDtoMapper = deviceDtoMapper;
    }

    public HomeDTO toDto(Home home) {
        HomeDTO homeDTO = new HomeDTO();
        homeDTO.setUuid(home.getUuid());
        homeDTO.setName(home.getName());
        homeDTO.setAddress(home.getAddress());
        homeDTO.setDevices(home.getDevices().stream().map(deviceDtoMapper::toDto).toList());
        return homeDTO;
    }

    public List<HomeDTO> toDto(List<Home> homes) {
        return homes.stream().map(this::toDto).toList();
    }

    public Home toEntity(HomeDTO homeDTO) {
        Home home = new Home();
        home.setUuid(homeDTO.getUuid());
        home.setName(homeDTO.getName());
        home.setAddress(homeDTO.getAddress());
        home.setDevices(homeDTO.getDevices().stream().map(deviceDtoMapper::toEntity).toList());
        return home;
    }

    public List<Home> toEntity(List<HomeDTO> homeDTOs) {
        return homeDTOs.stream().map(this::toEntity).toList();
    }
}
