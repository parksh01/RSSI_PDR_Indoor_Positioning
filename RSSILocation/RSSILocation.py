from math import sqrt

import numpy as np

# Type 1 : 일자배열 / Type 2 : 삼변측량
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

x_error_avg = []
y_error_avg = []

if type == '1':
    how_many_beacons = input("how many beacons? (2 / 3) : ")
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

            print('=== aspect coordinate : (' + str(i+1) + ', ' + str(j+1) + ') ===')
            """
            print(x)
            print(y)
            """

            x_avg = 0
            y_avg = 0
            if how_many_beacons == '3':
                x_avg = np.average(x)
                y_avg = np.average(y)
            elif how_many_beacons == '2':
                # beacon at (2,0) is omitted
                x_avg = x[1]
                y_avg = y[1]

            print('coord : (' + str(x_avg) + ', ' + str(y_avg) + ')')
            print('error : (' + str(abs((i + 1) - x_avg)) + ',' + str(abs((j + 1) - y_avg)) + ')')
            x_error_avg.append(abs((i + 1) - x_avg))
            y_error_avg.append(abs((j + 1) - y_avg))

            aspectDist = [sqrt((i+1)**2 + (j+1)**2), sqrt((i+1 - x1)**2 + (j+1)**2), sqrt( (i+1 - x2)**2 + (j+1)**2 )]
            measuredDist = [r1, r2, r3]
            errorDist = []
            for n in range(3):
                errorDist.append(abs(aspectDist[n] - measuredDist[n]))

            print('aspect distance : ', aspectDist)
            print('measured distance : ', measuredDist)
            print('error : ', errorDist)

            f.close()

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

            print('=== aspect coordinate : (' + str(i + 1) + ', ' + str(j + 1) + ') ===')

            """
            print(x)
            print(y)
            """

            x_avg = np.average(x)
            y_avg = np.average(y)

            print('coord : (' + str(x_avg) + ', ' + str(y_avg) + ')')
            print('error : (' + str(abs((i + 1) - x_avg)) + ',' + str(abs((j + 1) - y_avg)) + ')')
            x_error_avg.append(abs((i + 1) - x_avg))
            y_error_avg.append(abs((j + 1) - y_avg))

            aspectDist = [sqrt((i + 1) ** 2 + (j + 1) ** 2), sqrt((i + 1 - x1) ** 2 + (j + 1) ** 2), sqrt((i + 1) ** 2 + (j + 1 - y2) ** 2)]
            measuredDist = [r1, r2, r3]
            errorDist = []
            for n in range(3):
                errorDist.append(abs(aspectDist[n] - measuredDist[n]))

            print('aspect distance : ', aspectDist)
            print('measured distance : ', measuredDist)
            print('error : ', errorDist)

            f.close()

print()
print('avg coord error : ', np.average(x_error_avg), ',', np.average(y_error_avg))
print('coord error dev : ', np.std(x_error_avg), ',', np.std(y_error_avg))