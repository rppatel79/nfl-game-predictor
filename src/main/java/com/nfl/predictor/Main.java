package com.nfl.predictor;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting NFL ETL...");

        var etl = new NflEtl();
        etl.run();

        System.out.println("Done.");
    }
}
