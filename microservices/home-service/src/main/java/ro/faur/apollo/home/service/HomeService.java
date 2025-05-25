package ro.faur.apollo.home.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.faur.apollo.home.client.DeviceServiceClient;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.dto.DeviceDTO;
import ro.faur.apollo.home.dto.HomeDTO;
import ro.faur.apollo.home.mapper.HomeDtoMapper;
import ro.faur.apollo.home.repository.HomeRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HomeService {

    private static final Logger logger = LoggerFactory.getLogger(HomeService.class);
    private final HomeRepository homeRepository;
    private final DeviceServiceClient deviceServiceClient;
    private final HomeDtoMapper homeDtoMapper;

    public HomeService(HomeRepository homeRepository, DeviceServiceClient deviceServiceClient, HomeDtoMapper homeDtoMapper) {
        this.homeRepository = homeRepository;
        this.deviceServiceClient = deviceServiceClient;
        this.homeDtoMapper = homeDtoMapper;
    }

    @Transactional
    public HomeDTO createHome(String name, String address, String creatorUserUuid) {
        Home home = new Home(name, address);
        home = homeRepository.save(home);
        home.getAdminUuids().add(creatorUserUuid);
        home = homeRepository.save(home);
        return convertToDTO(home);
    }

    @Transactional
    public boolean deleteHome(String homeUuid, String userUuid) {
        if (!homeRepository.isUserAdminOfHome(userUuid, homeUuid)) {
            throw new IllegalArgumentException("User is not an admin of the home.");
        }
        Optional<Home> homeOpt = homeRepository.findById(homeUuid);
        if (homeOpt.isEmpty()) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        
        List<DeviceDTO> homeDevices = deviceServiceClient.getDevicesByHome(homeUuid);
        for (DeviceDTO device : homeDevices) {
            deviceServiceClient.unlinkDeviceFromHome(device.getUuid());
        }
        
        homeRepository.delete(homeOpt.get());
        return true;
    }

    @Transactional
    public List<HomeDTO> getHomesForUser(String userUuid) {
        List<Home> homes = homeRepository.findByAdminOrGuest(userUuid);
        return homes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public HomeDTO getHome(String homeUuid) {
        Optional<Home> homeOpt = homeRepository.findById(homeUuid);
        if (homeOpt.isEmpty()) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        return convertToDTO(homeOpt.get());
    }

    @Transactional
    public List<DeviceDTO> getHomeDevices(String homeUuid) {
        Optional<Home> homeOpt = homeRepository.findById(homeUuid);
        if (homeOpt.isEmpty()) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        return deviceServiceClient.getDevicesByHome(homeUuid);
    }

    @Transactional
    public DeviceDTO createDeviceInHome(String homeUuid, String name, String deviceType, String description, String hardwareId) {
        Optional<Home> homeOpt = homeRepository.findById(homeUuid);
        if (homeOpt.isEmpty()) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        
        logger.info("Calling device service to create device in home: homeUuid={}, name={}, deviceType={}, description={}, hardwareId={}",
                   homeUuid, name, deviceType, description, hardwareId);
        
        try {
            DeviceDTO createdDevice = deviceServiceClient.createDevice(name, deviceType, description, hardwareId, homeUuid);
            logger.info("Successfully created device in device service: {}", createdDevice.getUuid());
            
            Home home = homeOpt.get();
            home.getDeviceUuids().add(createdDevice.getUuid());
            homeRepository.save(home);
            
            logger.info("Successfully added device {} to home {}", createdDevice.getUuid(), homeUuid);
            return createdDevice;
        } catch (Exception e) {
            logger.error("Error calling device service to create device: {}", e.getMessage(), e);
            throw e; // To be handled by the controller
        }
    }

    @Transactional
    public boolean unlinkDeviceFromHome(String homeUuid, String deviceUuid) {
        Optional<Home> homeOpt = homeRepository.findById(homeUuid);
        if (homeOpt.isEmpty()) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        
        Home home = homeOpt.get();
        
        if (!home.getDeviceUuids().contains(deviceUuid)) {
            throw new IllegalArgumentException("Device does not belong to this home");
        }
        
        Boolean success = deviceServiceClient.unlinkDeviceFromHome(deviceUuid);
        
        if (success != null && success) {
            home.getDeviceUuids().remove(deviceUuid);
            homeRepository.save(home);
            return true;
        }
        
        return false;
    }

    public boolean isUserAdminOfHome(String userUuid, String homeUuid) {
        return homeRepository.isUserAdminOfHome(userUuid, homeUuid);
    }

    public boolean isUserGuestOfHome(String userUuid, String homeUuid) {
        return homeRepository.isUserGuestOfHome(userUuid, homeUuid);
    }

    /**
     * Convert Home entity to HomeDTO using the mapper
     */
    private HomeDTO convertToDTO(Home home) {
        try {
            List<DeviceDTO> devices = deviceServiceClient.getDevicesByHome(home.getUuid());
            return homeDtoMapper.toDto(home, devices);
        } catch (Exception e) {
            HomeDTO homeDTO = homeDtoMapper.toDto(home);
            homeDTO.setDevices(List.of());
            return homeDTO;
        }
    }
} 