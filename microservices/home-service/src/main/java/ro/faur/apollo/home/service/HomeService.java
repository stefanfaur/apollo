package ro.faur.apollo.home.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.faur.apollo.home.client.DeviceServiceClient;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.dto.DeviceDTO;
import ro.faur.apollo.home.dto.HomeDTO;
import ro.faur.apollo.home.mapper.HomeDtoMapper;
import ro.faur.apollo.home.repository.HomeRepository;
import ro.faur.apollo.home.dto.HomeSummaryDTO;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomeService {

    private static final Logger logger = LoggerFactory.getLogger(HomeService.class);
    private final HomeRepository homeRepository;
    private final DeviceServiceClient deviceServiceClient;
    private final HomeDtoMapper homeDtoMapper;
    private final TransactionTemplate transactionTemplate;

    public HomeService(HomeRepository homeRepository, DeviceServiceClient deviceServiceClient, HomeDtoMapper homeDtoMapper, PlatformTransactionManager transactionManager) {
        this.homeRepository = homeRepository;
        this.deviceServiceClient = deviceServiceClient;
        this.homeDtoMapper = homeDtoMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public HomeDTO createHome(String name, String address, String creatorUserUuid) {
        Home home = new Home(name, address);
        home.getAdminUuids().add(creatorUserUuid);
        home.getGuests();
        home.getDeviceUuids();
        Home saved = homeRepository.save(home);
        return homeDtoMapper.toDto(saved);
    }

    public boolean deleteHome(String homeUuid, String userUuid) {
        if (!homeRepository.isUserAdminOfHome(userUuid, homeUuid)) {
            throw new IllegalArgumentException("User is not an admin of the home.");
        }

        List<DeviceDTO> homeDevices = deviceServiceClient.getDevicesByHome(homeUuid);

        for (DeviceDTO device : homeDevices) {
            deviceServiceClient.unlinkDeviceFromHome(device.getUuid());
        }

        homeRepository.deleteById(homeUuid);
        return true;
    }

    public List<HomeDTO> getHomesForUser(String userUuid) {
        List<Home> homes = homeRepository.findByAdminOrGuest(userUuid);

        if (homes.isEmpty()) {
            return List.of();
        }

        homes.forEach(h -> {
            h.getDeviceUuids().size();
            h.getAdminUuids().size();
        });

        List<String> homeUuids = homes.stream()
                .map(Home::getUuid)
                .collect(Collectors.toList());

        List<DeviceDTO> allDevices = deviceServiceClient.getDevicesByHomeUuids(homeUuids);

        var devicesByHome = allDevices.stream()
                .collect(Collectors.groupingBy(DeviceDTO::getHomeUuid));

        return homes.stream()
                .map(home -> homeDtoMapper.toDto(home, devicesByHome.getOrDefault(home.getUuid(), List.of())))
                .collect(Collectors.toList());
    }

    public HomeDTO getHome(String homeUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found for UUID: " + homeUuid));

        home.getDeviceUuids().size();
        home.getAdminUuids().size();
        home.getGuests().size();

        return convertToDTO(home);
    }

    public List<DeviceDTO> getHomeDevices(String homeUuid) {
        if (!homeRepository.existsById(homeUuid)) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        return deviceServiceClient.getDevicesByHome(homeUuid);
    }

    public DeviceDTO createDeviceInHome(String homeUuid, String name, String deviceType, String description, String hardwareId) {
        if (!homeRepository.existsById(homeUuid)) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }

        logger.info("Calling device service to create device in home: homeUuid={}, name={}, deviceType={}, description={}, hardwareId={}",
                homeUuid, name, deviceType, description, hardwareId);

        DeviceDTO createdDevice = deviceServiceClient.createDevice(name, deviceType, description, hardwareId, homeUuid);

        logger.info("Successfully created device in device service: {}", createdDevice.getUuid());

        transactionTemplate.execute(status -> {
            Home home = homeRepository.findById(homeUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Home not found for UUID: " + homeUuid));
            home.getDeviceUuids().add(createdDevice.getUuid());
            homeRepository.save(home);
            return null;
        });

        return createdDevice;
    }

    public boolean unlinkDeviceFromHome(String homeUuid, String deviceUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found for UUID: " + homeUuid));

        if (!home.getDeviceUuids().contains(deviceUuid)) {
            throw new IllegalArgumentException("Device does not belong to this home");
        }

        Boolean success = deviceServiceClient.unlinkDeviceFromHome(deviceUuid);

        if (Boolean.TRUE.equals(success)) {
            transactionTemplate.execute(status -> {
                home.getDeviceUuids().remove(deviceUuid);
                homeRepository.save(home);
                return null;
            });
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

    @Cacheable(value = "homeSummaries", key = "#userUuid")
    public List<HomeSummaryDTO> getHomeSummariesForUser(String userUuid) {
        return homeRepository.findByAdminOrGuest(userUuid).stream()
                .map(h -> new HomeSummaryDTO(h.getUuid(), List.copyOf(h.getDeviceUuids())))
                .collect(Collectors.toList());
    }

    /**
     * Convert Home entity to HomeDTO using the mapper
     */
    private HomeDTO convertToDTO(Home home) {
        try {
            List<DeviceDTO> devices = deviceServiceClient.getDevicesByHomeUuids(List.of(home.getUuid()));
            return homeDtoMapper.toDto(home, devices);
        } catch (Exception e) {
            HomeDTO homeDTO = homeDtoMapper.toDto(home);
            homeDTO.setDevices(List.of());
            return homeDTO;
        }
    }
} 