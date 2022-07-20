import csv
import tensorflow as tf

def oneHotEncode(whichCategory, howMany):
    encoded = []
    for i in range(howMany):
        if i == whichCategory:
            encoded.append(1)
        else:
            encoded.append(0)
    return encoded

def read_csv(path):
    f = open(path, 'r')
    print("Now reading : " + path)
    rows = []
    rdr = csv.reader(f)
    linecount = 0
    for line in rdr:
        if linecount == 0:
            linecount = linecount + 1
        else:
            temp = []
            for val in line:
                temp.append(float(val.strip()))
            rows.append(temp)
            linecount = linecount + 1
    f.close()
    return rows

# sliceSize = 100, sliceNum = 100 will be suitable.
def sliceData(data, sliceSize):
    rows = len(data)
    startingPoints = range(0, rows - sliceSize)
    slicedData = []
    for i in startingPoints:
        slicedData.append(data[i:i+sliceSize])
    return slicedData

# path : array of paths of testcases
# category : category number of each testcase files
# sliceSize : slice dataset by this size (100 will be suitable)
# test_split : split testcases and training dataset
def load_data(path, category, sliceSize, test_split):
    X_train = []
    y_train = []
    X_test = []
    y_test = []
    for i in range(len(path)):
        slicedData = sliceData(read_csv(path[i]), sliceSize)
        testCases = int(len(slicedData) * test_split)
        X_train = X_train + slicedData[testCases:]
        y_train = y_train + (oneHotEncode(category[i], len(set(category))) * (len(slicedData) - testCases))
        X_test = X_test + slicedData[:testCases]
        y_test = y_test + (oneHotEncode(category[i], len(set(category))) * testCases)
    return (X_train, y_train), (X_test, y_test)
