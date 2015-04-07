# PredictionIO-Churn-Prediction-H2O-Sparkling-Water
PredictionIO Engine integrated with Sparkling Water. Open Source project Spring 2015 @CMU.

## Overview
This is an engine template for [PredictionIO](http://prediction.io/) with [Sparkling Water](https://github.com/h2oai/sparkling-water) integration. The goal is to use Deep Learning algorithm to predict the churn rate for a phone carrier's customers.

## Setup
* Please follow [Install PredictionIO](http://docs.prediction.io/install/) and [Download Engine Template](http://docs.prediction.io/start/download/) first for the initial setup.
* Run `pio-start-all` to start the PredictionIO environment.
* Run `pio app new [app-name]` to create a new application in PredictionIO.
* Update `engine.json` with the new App ID acquired from last step.
* Import the data set with `python data/import_eventserver.py --access_key [app-access-key]`. The data set should be downloaded [here](https://www.dropbox.com/s/ih68jrlk2tnqmbu/churn-orange-train.csv?dl=0) and kept in `data` directory.
* Configure Deep Learning algorithm parameters in engine.json.
* Run PredictionIO (`pio build && pio train && pio deploy`)

## Note
Sparkling Water library jar is downloaded with the template (in `lib` folder) instead of imported from build.sbt because the maven repository is outdated.
