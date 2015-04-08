# PredictionIO-Churn-Prediction-H2O-Sparkling-Water
PredictionIO Engine integrated with Sparkling Water. Open Source project Spring 2015 @CMU.

## Overview
This is an engine template for [PredictionIO](http://prediction.io/) with [Sparkling Water](https://github.com/h2oai/sparkling-water) integration. The goal is to use Deep Learning algorithm to predict the churn rate for a phone carrier's customers.

## Setup
* Please follow [Install PredictionIO](http://docs.prediction.io/install/) and [Download Engine Template](http://docs.prediction.io/start/download/) first for the initial setup.
* Run `pio-start-all` to start the PredictionIO environment.
* Run `pio app new [app-name]` to create a new application in PredictionIO.
* Update `engine.json` with the new App ID acquired from last step.
* Import the data set with `python data/import_eventserver.py --access_key [app-access-key]`.
* Configure Deep Learning algorithm parameters in engine.json.
* Run PredictionIO (`pio build && pio train && pio deploy`)


## Example
First, make sure that your engine is running. Then, in a Python shell, execute

```
 import predictionio
 engine_client = predictionio.EngineClient(url="http://localhost:8000")
```

in order to instantiate the engine client. After that, you can use `engine_client.send_query({"param1": value1, "param2": value2, ...})` to make predictions. In order to make predictions successfully, you have to specify the following parameters:

`intlPlan`, `voiceMailPlan`, `numVmailMsg`, `totalDayMins`, `totalDayCalls`, `totalDayCharge`, `totalEveMins`, `totalEveCalls`, `totalEveCharge`, `totalNightMins`, `totalNightCalls`, `totalNightCharge`, `totalIntlMins`, `totalIntlCalls`, `totalIntlCharge`, `customerServiceCalls`

An example prediction command could be:

```
engine_client.send_query({'intlPlan': True, 'voiceMailPlan': True, 'numVmailMsg': 41, 'totalDayMins': 173.1, 
    'totalDayCalls': 85, 'totalDayCharge': 29.43, 'totalEveMins': 203.9, 'totalEveCalls': 107, 
    'totalEveCharge': 17.33, 'totalNightMins': 122.2, 'totalNightCalls': 78, 'totalNightCharge': 14.02, 
    'totalIntlMins': 10.0, 'totalIntlCalls': 15, 'totalIntlCharge': 3.94, 'customerServiceCalls': 0})
```

## Note
Sparkling Water library jar is downloaded with the template (in `lib` folder) instead of imported from build.sbt because the maven repository is outdated.
