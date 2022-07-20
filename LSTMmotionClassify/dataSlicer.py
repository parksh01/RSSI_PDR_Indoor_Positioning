import pandas as pd
import random

# Pick distinct random numbers within the range.
def fragStartingPoint(numRange, pick):
    list = []
    ran_num = random.randint(0, numRange)
    for i in range(pick):
        while ran_num in list:
            ran_num = random.randint(0, numRange)
        list.append(ran_num)
    list.sort()
    return list

# Take input of pandas DataFrame array and slice it.
# sliceSize = 100, sliceNum = 100 will be suitable.
def sliceData(data, sliceSize, sliceNum):
    rows = data.shape[0]
    startingPoints = fragStartingPoint(rows - sliceSize, sliceNum)
    slicedData = []
    for i in startingPoints:
        slicedData.append(data[i:i+sliceSize])
    return slicedData

slicedData = sliceData(pd.read_csv("TrainingData/move/move1.csv"), 100, 100)

