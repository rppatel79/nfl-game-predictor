package com.nfl.predictor;


import org.tribuo.DataSource;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.evaluation.LabelEvaluation;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.data.csv.CSVLoader;
import org.tribuo.evaluation.TrainTestSplitter;
import org.tribuo.classification.LabelFactory;
import org.tribuo.Model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads the ETL output CSV, trains a simple classifier, and evaluates it.
 */
public class NflModelTrainer {

    private static final Path ETL_OUTPUT = Paths.get("build/nfl_games_for_ml.csv");

    /**
     * Trains a logistic regression model on the ETL output and evaluates it.
     *
     * @return LabelEvaluation containing metrics (accuracy, confusion matrix, etc.)
     */
    public LabelEvaluation trainAndEvaluate() {
        try {
            if (!Files.exists(ETL_OUTPUT)) {
                throw new IllegalStateException("ETL output not found at " + ETL_OUTPUT +
                        " – run NflEtl first.");
            }

            // 1) Load data
            var labelFactory = new LabelFactory();
            var loader = new CSVLoader<>(labelFactory);

            // "home_team_wins" is the label column in nfl_games_for_ml.csv
            DataSource<Label> dataSource = loader.loadDataSource(ETL_OUTPUT, "home_team_wins");

            // 2) Train/test split
            TrainTestSplitter<Label> splitter =
                    new TrainTestSplitter<>(dataSource, 0.7, 42L);

            MutableDataset<Label> train = new MutableDataset<>(splitter.getTrain());
            MutableDataset<Label> test  = new MutableDataset<>(splitter.getTest());

            if (train.size() == 0 || test.size() == 0) {
                throw new IllegalStateException("Train or test dataset is empty. " +
                        "Check ETL output and filtering logic.");
            }

            System.out.printf("Train size: %d, Test size: %d%n", train.size(), test.size());

            // 3) Trainer & model
            LogisticRegressionTrainer trainer = new LogisticRegressionTrainer();
            Model<Label> model = trainer.train(train);

            // 4) Evaluation
            LabelEvaluator evaluator = new LabelEvaluator();
            LabelEvaluation evaluation = evaluator.evaluate(model, test);

            double accuracy = evaluation.accuracy();
            System.out.println("=== NFL Winner Prediction Model Evaluation ===");
            System.out.printf("Accuracy: %.4f%n", accuracy);
            System.out.println("Confusion Matrix:\n" + evaluation.getConfusionMatrix());

            return evaluation;
        } catch (Exception e) {
            throw new RuntimeException("Error training and evaluating model", e);
        }
    }
}