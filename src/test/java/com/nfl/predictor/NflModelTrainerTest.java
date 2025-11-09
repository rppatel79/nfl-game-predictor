package com.nfl.predictor;

import org.junit.jupiter.api.Test;
import org.tribuo.classification.evaluation.LabelEvaluation;

import static org.junit.jupiter.api.Assertions.*;

class NflModelTrainerTest {

    @Test
    void canTrainAndEvaluateModelOnEtlOutput() {
        // Ensure ETL runs first (idempotent)
        NflEtl etl = new NflEtl();
        etl.run();

        NflModelTrainer trainer = new NflModelTrainer();
        LabelEvaluation eval = trainer.trainAndEvaluate();

        double accuracy = eval.accuracy();
        System.out.println("Test-run accuracy = " + accuracy);

        // Very loose sanity check – just ensure the model trains and has some non-zero accuracy.
        assertTrue(accuracy > 0.3, "Accuracy should be above a minimal sanity threshold");
    }
}