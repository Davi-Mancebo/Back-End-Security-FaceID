package com.example.backend.service;

import com.example.backend.model.DeviceModel;
import com.example.backend.repository.DeviceRepository;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    private final DeviceRepository repository;

    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    /**
     * Recupera o dispositivo pelo nome ou cria um novo registro com valores padrão.
     */
    public DeviceModel findOrCreate(String deviceName) {
        return repository.findByName(deviceName)
                .orElseGet(() -> repository.save(buildDefaultDevice(deviceName)));
    }

    private DeviceModel buildDefaultDevice(String deviceName) {
        DeviceModel device = new DeviceModel();
        device.setName(deviceName);
        device.setType("Unknown");
        device.setLocation("");
        return device;
    }
}
