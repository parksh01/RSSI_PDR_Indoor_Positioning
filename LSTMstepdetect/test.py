# Run the model with testcases.

from tensorflow.keras.models import Sequential, load_model
import numpy as np

def label(labels):
    label2num = {}
    num2label = {}
    n = 0
    for i in labels:
        label2num[i] = n
        num2label[n] = i
        n = n + 1
    return label2num, num2label

def load_testcases(path, sliceSize, label2num, dimension):
    # First, read file and store in array
    num_classes = len(label2num)
    f = open(path, 'r')
    print("Now reading : " + path)
    X_test = []
    y_test = []
    rdr = csv.reader(f)
    linecount = 0
    for line in rdr:
        if linecount == 0:
            linecount = linecount + 1
        else:
            temp = []
            for i in range(dimension):
                temp.append(float(line[i].strip()))
            X_test.append(temp)
            y_test.append(oneHotEncode(label2num[line[dimension]], num_classes))
            linecount = linecount + 1
    f.close()
    # then slice it.
    slicedData = sliceData(X_test, sliceSize)
    slicedLabel = []
    for i in range(0, linecount - sliceSize):
        slicedLabel.append(y_test[i])
    return np.array(slicedData), np.array(slicedLabel)

def oneHotEncode(whichCategory, howMany):
    encoded = []
    for i in range(howMany):
        if i == whichCategory:
            encoded.append(1)
        else:
            encoded.append(0)
    return encoded

def sliceData(data, sliceSize):
    rows = len(data)
    startingPoints = range(0, rows - sliceSize)
    slicedData = []
    for i in startingPoints:
        slicedData.append(data[i:i+sliceSize])
    return slicedData

# Categories.
label2num, num2label = label(['1', '0'])

# Prepare testcase csv file
sliceSize = 30
testcases_directory = '/content/drive/MyDrive/Colab Notebooks/testdata'
validation_directory = '/content/drive/MyDrive/Colab Notebooks/testdata/Validation'
testcases = ['accelLog - 2022-09-14-21시 31분 40초(processed)']

# Load the trained model.
model = load_model('/content/drive/MyDrive/Colab Notebooks/best_model.h5')

for filename in testcases:
    # Load each file
    (X_test, y_test) = load_testcases(testcases_directory + '/' + filename + '.csv', sliceSize, label2num, 3)

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