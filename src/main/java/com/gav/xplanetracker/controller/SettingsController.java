package com.gav.xplanetracker.controller;

import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import com.gav.xplanetracker.service.SettingsService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private final SettingsService settingsService;

    public SettingsController() {
        this.settingsService = SettingsService.getInstance();
    }

    @FXML
    protected TextField simbriefUsernameField;

    @FXML
    protected TextField xplaneUriField;

    @FXML
    protected RadioButton navigraphYes;

    @FXML
    protected RadioButton navigraphNo;

    @FXML
    protected Button saveButton;

    @FXML
    protected RadioButton screenshotsYes;

    @FXML
    protected RadioButton screenshotsNo;

    @FXML
    protected TextField screenshotDirectoryField;

    @FXML
    public void initialize() {
        final ApplicationSettingsDTO settings = settingsService.getSettings();
        simbriefUsernameField.setText(settings.simbriefUsername());
        xplaneUriField.setText(settings.xplaneHost());
        screenshotDirectoryField.setText(settings.xPlaneScreenshotDirectory());

        final ToggleGroup navigraphToggleGroup = new ToggleGroup();
        navigraphNo.setToggleGroup(navigraphToggleGroup);
        navigraphYes.setToggleGroup(navigraphToggleGroup);

        if (settings.useNavigraphApi()) {
            navigraphYes.setSelected(true);
            navigraphNo.setSelected(false);
        } else {
            navigraphYes.setSelected(false);
            navigraphNo.setSelected(true);
        }

        final ToggleGroup screenshotsToggleGroup = new ToggleGroup();
        screenshotsYes.setToggleGroup(screenshotsToggleGroup);
        screenshotsNo.setToggleGroup(screenshotsToggleGroup);

        if (settings.monitorScreenshots()) {
            screenshotsYes.setSelected(true);
            screenshotsNo.setSelected(false);
        } else {
            screenshotsYes.setSelected(false);
            screenshotsNo.setSelected(true);
        }
    }

    @FXML
    private void handleSaveClick() {
        final String simbriefUsername = simbriefUsernameField.getText();
        final String xplaneUri = xplaneUriField.getText();
        final boolean useNavigraph = navigraphYes.isSelected();
        final boolean monitorScreenshots = screenshotsYes.isSelected();
        final String screenshotDirectory = screenshotDirectoryField.getText();

        //TODO add validation

        final ApplicationSettingsDTO settings = new ApplicationSettingsDTO(
                simbriefUsername,
                xplaneUri,
                useNavigraph,
                monitorScreenshots,
                screenshotDirectory
        );

        settingsService.save(settings);
    }
}
