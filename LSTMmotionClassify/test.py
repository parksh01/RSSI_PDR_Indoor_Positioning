from tensorflow.keras.models import Sequential, load_model
from dataProcess import load_testcases, label
import numpy as np

label2num, num2label = label(["stop", "stopLeft", "stopRight", "move", "moveLeft", "moveRight"])

(X_test, y_test) = load_testcases('TestCase/Sensor Data - 2022-07-21-10시 54분 58초.csv', 100, label2num)
print(len(X_test))
print(len(y_test))

model = load_model('best_model.h5')
y_prediction = model.predict(X_test)

truecount = 0
falsecount = 0
for i in range(len(y_prediction)):
    pred = np.argmax(y_prediction[i])
    val = np.argmax(y_test[i])
    valid = 'n/a'
    if pred == val:
        valid = 'true'
        truecount = truecount + 1
    else:
        valid = 'false'
        falsecount = falsecount + 1
    print("prediction : " + str(num2label[pred]) + " / value : " + str(num2label[val]) + " / " + valid)
print('acc : ' + str(truecount / (truecount + falsecount)))