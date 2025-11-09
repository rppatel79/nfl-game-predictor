package com.nfl.predictor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.classification.Label;
import org.tribuo.data.csv.CSVLoader;

/**
 * Simple smoke test to confirm Tribuo libraries are available on the classpath.
 */
class TribuoLibraryLoadTest {

    @Test
    void tribuoClassesShouldBeLoadable() {
        try {
            // Try to instantiate a few core Tribuo components
            var trainer = new LogisticRegressionTrainer();
            var loader = new CSVLoader<>(new org.tribuo.classification.LabelFactory());
            var label = new Label("win");

            // If we reached here, Tribuo classes loaded successfully
            assertNotNull(trainer);
            assertNotNull(loader);
            assertNotNull(label);

            System.out.println("✅ Tribuo libraries loaded successfully.");
        } catch (Throwable t) {
            fail("Tribuo classes could not be loaded: " + t.getMessage());
        }
    }
}