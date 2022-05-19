from math import sqrt

import numpy as np

type = input("Type? 1 / 2 : ")

def coorAdd(coor1, coor2):
    return [coor1[0] + coor2[0], coor1[1] + coor2[1]]

def coorSub(coor1, coor2):
    return [coor1[0] - coor2[0], coor1[1] - coor2[1]]

r1 = 0
r2 = 0
r3 = 0

x = []
y = []

if type == '1':
    for i in range(3):
        for j in range(3):
            # aspect coordination is (i, j)
            f = open("Type 1/Distance Log - " + str(i+1) + str(j+1) + '.csv', 'r')
            r1 = 0
            r2 = 0
            r3 = 0
            x.clear()
            y.clear()
            tick = 0
            line = f.readline()
            while True:
                line = f.readline()
                processLine = line.split(',')
                if not line:
                    break
                if (processLine[2] != 'null' and processLine[3] != 'null' and processLine[4] != 'null\n'):
                    r1 = r1 + float(processLine[2])
                    r2 = r2 + float(processLine[3])
                    r3 = r3 + float(processLine[4])
                    tick = tick + 1

            r1 = r1 / tick
            r2 = r2 / tick
            r3 = r3 / tick

            x1 = 2.0
            x2 = 4.0

            # Beacon 01 and Beacon 02
            x.append((r2**2 - r1**2 - x1**2)/(-2*x1))
            y.append(sqrt(abs(r1**2 - x[0]**2)))

            # Beacon 01 and Beacon 03
            x.append((r3**2 - r2**2 - x2**2 + x1**2)/(-2*(x2-x1)))
            y.append(sqrt(abs(r2**2 - (x1 - x[1])**2)))

            # Beacon 02 and Beacon 03
            x.append((r3**2 - r1**2 - x2**2)/(-2*x2))
            y.append(sqrt(abs(r1**2 - x[2]**2)))

            print('=== aspect coordinate : (' + str(i+1) + ', ' + str(j+1) + ')')

            print(x)
            print(y)

            print('coord : (' + str(np.average(x)) + ', ' + str(np.average(y)) + ')')
            print('error : (' + str(abs((i+1)-np.average(x))) + ',' + str(abs((j+1)-np.average(y))) + ')')

elif type == '2':
    for i in range(3):
        for j in range(3):
            # aspect coordination is (i, j)
            f = open("Type 2/Distance Log - " + str(i + 1) + str(j + 1) + '.csv', 'r')
            r1 = 0
            r2 = 0
            r3 = 0
            x.clear()
            y.clear()
            tick = 0
            line = f.readline()
            while True:
                line = f.readline()
                processLine = line.split(',')
                if not line:
                    break
                if (processLine[2] != 'null' and processLine[3] != 'null' and processLine[4] != 'null\n'):
                    r1 = r1 + float(processLine[2])
                    r2 = r2 + float(processLine[3])
                    r3 = r3 + float(processLine[4])
                    tick = tick + 1

            r1 = r1 / tick
            r2 = r2 / tick
            r3 = r3 / tick

            x1 = 4.0
            y2 = 4.0

            # Beacon 01 and Beacon 02
            x.append((r2**2 - r1**2 - x1**2)/(-2*x1))
            y.append(sqrt(abs(r1**2 - x[0]**2)))

            # Beacon 01 and Beacon 03
            y.append((r3**2 - r1**2 - y2**2)/(-2*y2))
            x.append(sqrt(abs(r1**2-y[1]**2)))

            print('=== aspect coordinate : (' + str(i + 1) + ', ' + str(j + 1) + ')')

            print(x)
            print(y)

            print('coord : (' + str(np.average(x)) + ', ' + str(np.average(y)) + ')')
            print('error : (' + str(abs((i + 1) - np.average(x))) + ',' + str(abs((j + 1) - np.average(y))) + ')')
