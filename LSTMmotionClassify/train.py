import pandas as pd
from dataProcess import load_data
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, LSTM, Embedding
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import numpy as np

# Training Datasets
path = ["TrainingData/stop/stop.csv",
        "TrainingData/stopLeft/stopLeft.csv",
        "TrainingData/stopRight/stopRight.csv",
        "TrainingData/move/move1.csv",
        "TrainingData/move/move2.csv",
        "TrainingData/moveLeft/moveLeft.csv",
        "TrainingData/moveRight/moveRight.csv"]

# Category
# 0 - stop
# 1 - stopLeft
# 2 - stopRight
# 3 - move
# 4 - moveLeft
# 5 - moveRight
category = [0, 1, 2, 3, 3, 4, 5]

# loading training dataset
slice = 100
num_classes = len(set(category))

(X_train, y_train), (X_test, y_test) = load_data(path, category, slice, 0.3)
X_train = np.array(X_train).reshape(-1, slice, num_classes)
y_train = np.array(y_train).reshape(-1, num_classes)
X_test = np.array(X_test).reshape(-1, slice, num_classes)
y_test = np.array(y_test).reshape(-1, num_classes)

# Make model
# since some values are smaller than zero, use tanh, not relu.
model = Sequential()
model.add(LSTM(128, input_shape=(slice, num_classes), activation = 'tanh', return_sequences=True))
model.add(LSTM(256, input_shape=(128,), activation = 'tanh'))
model.add(Dense(num_classes, input_shape=(slice, num_classes), activation = 'softmax'))
model.summary()
es = EarlyStopping(monitor='loss', patience=5, mode='auto')
mc = ModelCheckpoint('best_model.h5', monitor='val_acc', mode='max', verbose=1, save_best_only=True)
model.compile(loss='categorical_crossentropy', optimizer='sgd', metrics=['acc'])

history = model.fit(X_train, y_train, batch_size=50, epochs=30, callbacks=[es, mc], validation_data=(X_test, y_test))