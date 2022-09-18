# Preprocessing training data
import csv
import numpy as np

# path : Input file path
# amount : How much to duplicate label '1'?
def preprocess(path, amount):
    f = open(path, 'r')
    out = open(path[:-4] + '(processed).csv', 'w', newline='')
    wr = csv.writer(out)
    count = 0
    isDuplicating = False
    print("Now pre-processing : " + path)
    rdr = csv.reader(f)
    for line in rdr:
        if line[-1] == '1':
            isDuplicating = True
            wr.writerow(line)
        else:
            if isDuplicating:
                line[-1] = '1'
                count = count + 1
                if count >= amount:
                    isDuplicating = False
                    count = 0
            wr.writerow(line)


path = ["/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 20분 44초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 36분 23초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 37분 58초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-13시 39분 05초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 15분 20초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 16분 35초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 17분 51초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 19분 17초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 20분 34초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 22분 02초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 23분 25초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 24분 44초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-14-21시 27분 43초.csv",
        "/content/drive/MyDrive/Colab Notebooks/traindata/accelLog - 2022-09-17-17시 48분 13초.csv",
        "/content/drive/MyDrive/Colab Notebooks/testdata/accelLog - 2022-09-14-21시 31분 40초.csv"]


for i in path:
    preprocess(i, 10)