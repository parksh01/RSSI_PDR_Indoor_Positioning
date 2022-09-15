from dataProcess import load_data
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, LSTM, Embedding
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import numpy as np

# Training Datasets
# Category
# 0 - not a step
# 1 - stepped
path = ["traindata/accelLog - 2022-09-14-13시 20분 44초.csv",
        "traindata/accelLog - 2022-09-14-13시 36분 23초.csv",
        "traindata/accelLog - 2022-09-14-13시 37분 58초.csv",
        "traindata/accelLog - 2022-09-14-13시 39분 05초.csv",
        "traindata/accelLog - 2022-09-14-21시 15분 20초.csv",
        "traindata/accelLog - 2022-09-14-21시 16분 35초.csv",
        "traindata/accelLog - 2022-09-14-21시 17분 51초.csv",
        "traindata/accelLog - 2022-09-14-21시 19분 17초.csv",
        "traindata/accelLog - 2022-09-14-21시 20분 34초.csv",
        "traindata/accelLog - 2022-09-14-21시 22분 02초.csv",
        "traindata/accelLog - 2022-09-14-21시 23분 25초.csv",
        "traindata/accelLog - 2022-09-14-21시 24분 44초.csv",
        "traindata/accelLog - 2022-09-14-21시 27분 43초.csv"]

# loading training dataset
slice = 30
(X_train, y_train), (X_test, y_test) = load_data(path, slice, 0.3)

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
model.add(LSTM(256, input_shape=(128,), activation = 'tanh'))
model.add(Dense(2, input_shape=(slice, 2), activation = 'softmax'))
model.summary()

# To prevent overfitting.
es = EarlyStopping(monitor='loss', patience=5, mode='auto')
mc = ModelCheckpoint('best_model.h5', monitor='val_acc', mode='max', verbose=1, save_best_only=True)

# Begin trainning.
model.compile(loss='categorical_crossentropy', optimizer='sgd', metrics=['acc'])
history = model.fit(X_train, y_train, batch_size=50, epochs=30, callbacks=[es, mc], validation_data=(X_test, y_test))

# Make Log
f = open("ML Log.txt", 'w')
f.write('loss\n')
f.write(history.history['loss'])
f.write('\n')

f.write('acc\n')
f.write(history.history['acc'])
f.write('\n')

f.write('val_loss\n')
f.write(history.history['val_loss'])
f.write('\n')

f.write('val_acc\n')
f.write(history.history['val_acc'])
f.write('\n')

f.close()