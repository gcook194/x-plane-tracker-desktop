package com.gav.xplanetracker.dao;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.exceptions.SettingsPropertyNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDaoJDBC {

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
            System.out.println("Could not get Navigraph Settings from database");
            e.printStackTrace();

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
            System.out.println("Could not get Simbrief username from database");
            e.printStackTrace();

            throw new SettingsPropertyNotFoundException("Could not get Simbrief username from database");
        }
    }
}
