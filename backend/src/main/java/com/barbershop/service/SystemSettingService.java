package com.barbershop.service;

import com.barbershop.model.SystemSetting;
import com.barbershop.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    public String getSetting(String key, String defaultValue) {
        return systemSettingRepository.findById(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }
    
    public void updateSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findById(key)
                .orElse(new SystemSetting(key, value));
        setting.setSettingValue(value);
        systemSettingRepository.save(setting);
    }

    public Map<String, String> getAllSettings() {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemSetting s : settings) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        return map;
    }

    public void updateSettings(Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue());
        }
    }
}
