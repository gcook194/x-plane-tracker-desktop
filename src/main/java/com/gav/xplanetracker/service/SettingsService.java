package com.gav.xplanetracker.service;

import com.gav.xplanetracker.dao.SettingsDaoJDBC;
import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsService {

    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    private static SettingsService INSTANCE;

    private final SettingsDaoJDBC settingsDao;

    public static SettingsService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SettingsService();
        }

        return INSTANCE;
    }

    public SettingsService() {
        this.settingsDao = SettingsDaoJDBC.getInstance();
    }

    public boolean useNavigraphConnection() {
        return settingsDao.useNavigraphConnection();
    }

    public String getSimbriefUsername() {
        return settingsDao.getSimbriefUsername();
    }

    public ApplicationSettingsDTO getSettings() {
        return settingsDao.getSettings();
    }

    public void save(ApplicationSettingsDTO settings) {
        logger.info("saving settings: {}", settings.toString());
        settingsDao.save(settings);
    }
}
