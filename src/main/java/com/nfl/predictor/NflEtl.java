package com.nfl.predictor;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NflEtl {

    // Input and output file names
    private static final String INPUT_RESOURCE = "/data/raw_nfl_games.csv";
    private static final String OUTPUT_FILE = "build/nfl_games_for_ml.csv";

    private final Map<String, Integer> teamNumericMap = new HashMap<>();

    public void run() {
        try {
            loadTeamMap();

            // Load input CSV from resources
            InputStream is = getClass().getResourceAsStream(INPUT_RESOURCE);
            if (is == null) {
                throw new IllegalStateException("Could not find resource: " + INPUT_RESOURCE);
            }

            try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
                String[] header = reader.readNext();
                if (header == null) {
                    throw new IllegalStateException("Empty CSV file");
                }

                // Map column name -> index for convenience
                Map<String, Integer> idx = buildIndexMap(header);

                // Match your Kaggle columns exactly
                String seasonCol     = "schedule_season";
                String weekCol       = "schedule_week";
                String homeTeamCol   = "team_home";
                String awayTeamCol   = "team_away";
                String homeScoreCol  = "score_home";
                String awayScoreCol  = "score_away";

                // Validate required columns exist
                for (String col : new String[]{seasonCol, weekCol, homeTeamCol,
                        awayTeamCol, homeScoreCol, awayScoreCol}) {
                    if (!idx.containsKey(col)) {
                        throw new IllegalStateException("Missing expected column in CSV: " + col);
                    }
                }

                // Ensure output directory exists
                Path outPath = Path.of(OUTPUT_FILE);
                Files.createDirectories(outPath.getParent());

                try (CSVWriter writer = new CSVWriter(new FileWriter(outPath.toFile()))) {
                    // Output header
                    String[] outHeader = {
                            "season",
                            "home_team_id",
                            "away_team_id",
                            "home_score",
                            "away_score",
                            "home_team_wins"
                    };
                    writer.writeNext(outHeader);

                    String[] row;
                    int count = 0;
                    while ((row = reader.readNext()) != null) {

                        String season   = row[idx.get(seasonCol)];
                        String week     = row[idx.get(weekCol)];
                        String homeTeam = row[idx.get(homeTeamCol)];
                        String awayTeam = row[idx.get(awayTeamCol)];

                        int homeScore = parseIntSafe(row[idx.get(homeScoreCol)]);
                        int awayScore = parseIntSafe(row[idx.get(awayScoreCol)]);

                        int homeTeamNumeric = teamNumericMap.get(homeTeam);
                        int awayTeamNumeric = teamNumericMap.get(awayTeam);

                        // Compute label: 1 if home team wins, 0 if they lose, skip ties for now
                        if (homeScore == awayScore) {
                            // You can keep these if you want, but skipping keeps label strictly 0/1
                            continue;
                        }
                        int homeTeamWins = homeScore > awayScore ? 1 : 0;

                        String[] outRow = {
                                season,
                                String.valueOf(homeTeamNumeric),
                                String.valueOf(awayTeamNumeric),
                                String.valueOf(homeScore),
                                String.valueOf(awayScore),
                                String.valueOf(homeTeamWins)
                        };
                        writer.writeNext(outRow);
                        count++;
                    }

                    System.out.println("Wrote " + count + " games to " + OUTPUT_FILE);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error running ETL", e);
        }
    }

    private void loadTeamMap() {
        try (var reader = Files.newBufferedReader(Path.of("src/main/resources/data/nfl_teams.csv"))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] parts = line.split(",");
                String teamId = parts[1].trim();
                int numericId = Integer.parseInt(parts[0].trim());
                teamNumericMap.put(teamId, numericId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading team map", e);
        }
    }


    private Map<String, Integer> buildIndexMap(String[] header) {
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            idx.put(header[i], i);
        }
        System.out.println("CSV header: " + Arrays.toString(header));
        return idx;
    }

    private int parseIntSafe(String s) {
        return Integer.parseInt(s.trim());
    }
}
