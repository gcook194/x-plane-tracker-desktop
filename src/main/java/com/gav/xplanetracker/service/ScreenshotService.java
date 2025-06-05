package com.gav.xplanetracker.service;

import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.util.FileUtil;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScreenshotService {

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotService.class);

    private static ScreenshotService INSTANCE;

    private final String[] EXTENSIONS = new String[] {
            "gif", "png", "jpg", "jpeg"
    };

    private final FilenameFilter IMAGE_FILTER = (dir, name) -> {
        for (final String ext : EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return (true);
            }
        }
        return (false);
    };

    public static ScreenshotService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScreenshotService();
        }

        return INSTANCE;
    }

    // TODO also need to fix the access denied issue with reading x-plane files
    public void copyNewScreenshots(Flight flight, String xPlaneScreenshotDir) {
        final String screenshotDir = FileUtil.getApplicationDataPath() + "/screenshots/" + flight.getId();

        final Path trackerScreenshots = Paths.get(screenshotDir);

        if (!trackerScreenshots.toFile().exists()) {
            logger.info("creating screenshot dir for flight {}", flight.getId());
            trackerScreenshots.toFile().mkdir();
        }

        final Path xPlaneScreenshots = Paths.get(xPlaneScreenshotDir);

        if (xPlaneScreenshots.toFile().isDirectory()) {
            Arrays.stream(xPlaneScreenshots.toFile().listFiles(IMAGE_FILTER))
                    .forEach(file -> {
                        try (OutputStream outputStream = new FileOutputStream(screenshotDir)) {
                            final BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                            final FileTime created = attributes.creationTime();

                            // TODO also need to check that file has not already been copied.
                            if (created.toInstant().isAfter(flight.getStartedAt())) {
                                logger.info("Adding screenshot {} to flight {} directory", file.getName(), flight.getId());
                                final byte[] fileBytes = Files.readAllBytes(file.toPath());
                                outputStream.write(fileBytes);
                            }
                        } catch (IOException e) {
                            logger.error("Could not read file attributes for file: {}", file.getAbsolutePath(), e);
                        }
                    });
        }

    }

    public List<Image> getScreenshots(Flight flight) {
        final File directory = getScreenshotsFromStorage(flight);

        if (directory.exists() && directory.isDirectory()) {
            return Arrays.stream(directory.listFiles(IMAGE_FILTER))
                    .map(ScreenshotService::createImageObject)
                    .toList();
        }

        return Collections.emptyList();
    }

    private static File getScreenshotsFromStorage(Flight flight) {
        final String screenshotDirectory =
                FileUtil.getApplicationDataPath() + "/screenshots/" + flight.getId();

        return new File(screenshotDirectory);
    }

    private static Image createImageObject(File file) {
        try {
            return new Image(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
