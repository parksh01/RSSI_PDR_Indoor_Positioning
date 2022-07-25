from tensorflow.keras.models import Sequential, load_model
from dataProcess import load_testcases, label
import numpy as np

# Categories.
label2num, num2label = label(["stop", "stopLeft", "stopRight", "move", "moveLeft", "moveRight"])

# Prepare testcase csv file
sliceSize = 30
testcases_directory = 'TestCase'
validation_directory = 'TestCase/Validation'
testcases = ['testcase1',
             'testcase2']

# Load the trained model.
model = load_model('best_model.h5')

for filename in testcases:
    # Load each file
    (X_test, y_test) = load_testcases(testcases_directory + '/' + filename + '.csv', sliceSize, label2num)

    # Predicts with the model.
    y_prediction = model.predict(X_test)

    # Validation.
    # Compares predicted label and correct label.
    # Generates validation log file.
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
    outputFile = open(validation_directory + '/' + filename + " (txt).csv", 'w')
    outputFile.write(txt)
    outputFile.close()
    outputFile = open(validation_directory + '/' + filename + " (num).csv", 'w')
    outputFile.write(num)
    outputFile.close()
    print('Processed : ' + filename + '.csv')