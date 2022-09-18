from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, LSTM, Embedding
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import numpy as np
import csv

def read_csv(path, col):
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
            for i in range(col):
                temp.append(float(line[i].strip()))
            rows.append(temp)
            linecount = linecount + 1
    f.close()
    return rows

def sliceData_withLabel(data, sliceSize):
    rows = len(data)
    startingPoints = range(0, rows - sliceSize)
    slicedData = []
    slicedLabel = []
    for i in startingPoints:
        temp1 = []
        for j in range(sliceSize):
            temp1.append(data[i+j][:-1])
        slicedData.append(temp1)
        if (data[i][-1] == 1.0):
            slicedLabel.append([1, 0])
        elif (data[i][-1] == 0.0):
            slicedLabel.append([0, 1])
    if(len(slicedData) == len(slicedLabel)):
        return slicedData, slicedLabel
    else:
        print("size does not match")

def load_data(path, sliceSize, test_split):
    X_train = []
    y_train = []
    X_test = []
    y_test = []
    for i in range(len(path)):
        slicedData, slicedLabel = sliceData_withLabel(read_csv(path[i], 4), sliceSize)
        testCases = int(len(slicedData) * test_split)
        X_train = X_train + slicedData[testCases:]
        y_train = y_train + slicedLabel[testCases:]
        X_test = X_test + slicedData[:testCases]
        y_test = y_test + slicedLabel[:testCases]
    if len(X_train) == len(y_train):
        print("train size match : ", len(X_train))
    if len(X_test) == len(y_test):
        print("test size match : ", len(X_test))
    return (X_train, y_train), (X_test, y_test)

# Training Datasets
# Category
# 0 - not a step
# 1 - stepped
path = ["/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 20분 44초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 36분 23초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 37분 58초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 39분 05초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 15분 20초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 16분 35초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 17분 51초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 19분 17초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 20분 34초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 22분 02초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 23분 25초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 24분 44초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 27분 43초(processed).csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-17-17시 48분 13초(processed).csv"]

# loading training dataset
slice = 30
(X_train, y_train), (X_test, y_test) = load_data(path, slice, 0.2)

print(np.array(X_train).shape)
print(np.array(y_train).shape)
print(np.array(X_test).shape)
print(np.array(y_test).shape)
"""
X_train = np.array(X_train).reshape(-1, slice, 2)
y_train = np.array(y_train).reshape(-1, 2)
X_test = np.array(X_test).reshape(-1, slice, 2)
y_test = np.array(y_test).reshape(-1, 2)
"""
# Make model
# since some values are smaller than zero, use tanh, not relu.
model = Sequential()
model.add(LSTM(128, input_shape=(slice, 3), activation = 'tanh', return_sequences=True))
model.add(LSTM(256, input_shape=(64,), activation = 'tanh'))
model.add(Dense(2, input_shape=(slice, 2), activation = 'softmax'))
model.summary()

# To prevent overfitting.
es = EarlyStopping(monitor='loss', patience=5, mode='auto')
mc = ModelCheckpoint('/content/drive/MyDrive/Colab Notebooks/best_model.h5', monitor='val_acc', mode='max', verbose=1, save_best_only=True)

# Begin trainning.
model.compile(loss='categorical_crossentropy', optimizer='sgd', metrics=['acc'])
history = model.fit(X_train, y_train, batch_size=50, epochs=30, callbacks=[es, mc], validation_data=(X_test, y_test))

# Make Log
print('loss')
print(history.history['loss'])

print('acc')
print(history.history['acc'])

print('val_loss')
print(history.history['val_loss'])

print('val_acc')
print(history.history['val_acc'])