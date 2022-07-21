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
txt = "prediction,value,valid\n"
num = "prediction,value,valid\n"
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
    print("prediction : " + str(num2label[pred]) + " / value : " + str(num2label[val]) + " - " + valid)
    txt += str(num2label[pred])
    txt += ','
    txt += str(num2label[val])
    txt += ','
    txt += valid
    txt += '\n'
    num += str(pred)
    num += ','
    num += str(val)
    num += ','
    if valid == 'true':
        num += str(len(label2num) - 1)
    elif valid == 'false':
        num += '0'
    num += '\n'
print('acc : ' + str(truecount / (truecount + falsecount)))
outputFile = open("Sensor Data - 2022-07-21-10시 54분 58초 (txt).csv", 'w')
outputFile.write(txt)
outputFile.close()
outputFile = open("Sensor Data - 2022-07-21-10시 54분 58초 (num).csv", 'w')
outputFile.write(num)
outputFile.close()