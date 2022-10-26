# RSSI_PDR_Indoor_Positioning

AccelComplFilt
- Kalman Filter implemented in Python (for experiment purpose)

AccelLocation
- Dead Reckoning App with direct values of accelerometer and gyroscope

Bluetooth Beacon (Arduino)
- Code for making beacon with Arduino

LSTMmotionClassify
- Trained neural network for classifying motions from the values of accelerometer and gyroscope

LSTMmoveClassify
- A test app which classifies motions, with the model trained with 'LSTMmotionClassify' (for experiment purpose)

LSTMstepdetect
- Trained neural network for detecting motions from the values of accelerometer

PathVisualize
- Visualized PDR log files with matplotlib (for experiment purpose)

PDRbyStep
- A test app which performs PDR by step detection which utilizes the algorithm based on detecting sensor value threshold (for experiment purpose)

PythonParticleFilter
- Particle filter implemented in Python (for experiment purpose)

RSSICurveFitting
- Performed curve fitting with measured RSSI values at certain positions, and found n/A values for RSSI-to-distance mapping function

RSSILocation
- Finds coordinates from log files which is recorded at certain coordinates (for experiment purpose)

RSSIMeasureApp
- Measures Kalman filtered RSSI and turn it into the distance, and performs Particle filtering for getting the coordinates (Final Application)

StepDetectionLSTMapp
- Performs PDR with ML model from 'LSTMstepdetect', and step detection is controlled by the ML model from 'LSTMmotionClassify' (Final Application)
