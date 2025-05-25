package com.gav.xplanetracker.dao;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import com.gav.xplanetracker.exceptions.SettingsPropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDaoJDBC {

    private static final Logger logger = LoggerFactory.getLogger(SettingsDaoJDBC.class);

    private static SettingsDaoJDBC INSTANCE;

    public static SettingsDaoJDBC getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SettingsDaoJDBC();
        }
        return INSTANCE;
    }

    public boolean useNavigraphConnection() {
        final String SQL = "SELECT use_navigraph_api FROM settings";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            final ResultSet rs = ps.executeQuery();

            return rs.getBoolean("use_navigraph_api");
        } catch (SQLException e) {
            logger.error("Could not get Navigraph Settings from database", e);

            throw new SettingsPropertyNotFoundException("Could not get Navigraph API settings from database");
        }
    }

    public String getSimbriefUsername() {
        final String SQL = "SELECT simbrief_username FROM settings";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            final ResultSet rs = ps.executeQuery();

            return rs.getString("simbrief_username");
        } catch (SQLException e) {
            logger.error("Could not get Simbrief username from database", e);

            throw new SettingsPropertyNotFoundException("Could not get Simbrief username from database");
        }
    }

    public ApplicationSettingsDTO getSettings() {
        final String SQL = "SELECT * FROM settings";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            final ResultSet rs = ps.executeQuery();

            final String simbriefUsername = rs.getString("simbrief_username");
            final boolean useSimbriefApi = rs.getBoolean("use_navigraph_api");
            final String xPlaneHost = rs.getString("x_plane_host");

            return new ApplicationSettingsDTO(
                    simbriefUsername,
                    xPlaneHost,
                    useSimbriefApi
            );
        } catch (SQLException e) {
            logger.error("Could not get settings from database from database", e);

            throw new SettingsPropertyNotFoundException("Could not get Simbrief username from database");
        }
    }

    public void save(ApplicationSettingsDTO settings) {
        final String SQL = "UPDATE settings SET simbrief_username = ?, use_navigraph_api = ?";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, settings.simbriefUsername());
            ps.setBoolean(2, settings.useNavigraphApi());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Could not update settings from database from database", e);

            throw new SettingsPropertyNotFoundException("Could not get Simbrief username from database");
        }
    }
}
