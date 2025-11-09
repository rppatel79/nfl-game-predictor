package com.nfl.predictor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

class NflEtlTest {

    @Test
    void etlShouldProduceValidOutput() throws Exception {
        // Arrange
        NflEtl etl = new NflEtl();

        // Act
        etl.run();

        // Assert
        Path outputPath = Path.of("build/nfl_games_for_ml.csv");
        assertTrue(Files.exists(outputPath), "Output CSV should exist");

        try (BufferedReader reader = new BufferedReader(new FileReader(outputPath.toFile()))) {
            String header = reader.readLine();
            assertNotNull(header, "Output CSV should have a header");
            assertTrue(header.contains("home_team_wins"), "Header should include 'home_team_wins'");

            // Read first data row
            String firstRow = reader.readLine();
            assertNotNull(firstRow, "Output CSV should have at least one data row");

            // Optionally, ensure columns count is correct
            String[] cols = firstRow.split(",");
            assertEquals(7, cols.length, "Each row should have 7 columns");
        }
    }
}
