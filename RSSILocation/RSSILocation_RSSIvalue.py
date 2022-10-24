# RSSILocation Refactored
import math

import numpy as np

class Beacon:
    def __init__(self, addr, A_front, n_front, A_back, n_back, isBack, x, y, z):
        self.addr = addr
        self.A_front = A_front
        self.n_front = n_front
        self.A_back = A_back
        self.n_back = n_back
        self.isBack = isBack
        self.x = x
        self.y = y
        self.z = z
    def getDist(self, rssi):
        if not self.isBack:
            return 10**((rssi - self.A_front)/(-10 * self.n_front))
        elif self.isBack:
            return 10**((rssi - self.A_back)/(-10 * self.n_back))

def printError(distErrorX, distErrorY, keyword):
    print('평균 오차 : (' + str(round(np.average(distErrorX), 3)) + ',' + str(round(np.average(distErrorY), 3)) + ') / ' + '표준편차 : (' + str(round(np.std(distErrorX), 3)) + ',' + str(round(np.std(distErrorY), 3)) + ')')

def errorRatio(x1, x2, xerror1, xerror2):
    return (x1*xerror2 + x2*xerror1)/(xerror1+xerror2)

# pre defined variables (beacon info and log file locations)
foldername = 'Measure20221022/'
filename = 'c'
beacons = []

# start of algorithm
beacons.append(Beacon('A81B6AAE5FF6', -53.715, 2.021, -66.697, 0.924, False, x=0.0, y=5.0, z=0.0))
beacons.append(Beacon('A81B6AAE4C6B', -52.703, 1.956, -62.392, 1.627, True, x=2.5, y=0.0, z=0.0))
beacons.append(Beacon('A81B6AAE5260', -54.065, 1.751, -64.114, 1.565, False, x=5.0, y=5.0, z=0.0))
measureType = input("type? 1 / 2 / 3 : ")
if measureType == '1':
    foldername += '직각'
elif measureType == '2':
    foldername += '정삼각형'
elif measureType == '3':
    beacons[0].isBack = True
    beacons[1].isBack = True
    beacons[2].isBack = False
    beacons.append(Beacon('606405D13F7E', -57.781, 1.61, -50.846, 1.7, False, x=5.0, y=5.0))
    foldername += '4beacons'

coord = [5.0, 5.0]

beaconRSSI = []
beaconDist = []
distErrorX = []
distErrorY = []
for i in range(1, int(coord[1])):
    for j in range(1, int(coord[0])):
        # processing coordinate (j, i)
        beaconDistTemp = []
        beaconNum = -1
        x, y, z = [], [], []

        # First, get RSSI and distance info from each beacons.
        for beaconInfo in beacons:
            beaconNum += 1
            f = open(foldername + '/' + filename + str(j) + str(i) + '/' + beaconInfo.addr + '.csv', 'r')
            while True:
                # use Kalman filtered average values
                line = f.readline()
                processLine = line.split(',')
                if not line:
                    break
                elif processLine[0] == 'avg(rssi)':
                    line = f.readline()
                    processLine = line.split(',')
                    # use value of avg(rssiKalman)
                    beaconRSSI.append(float(processLine[1]))
                    beaconDistTemp.append(beaconInfo.getDist(float(processLine[1])))
                    break
        beaconDist.append(beaconDistTemp)

        # then calculate the coordinate.
        if measureType == '1': # 직각삼각형 배열
            x.append((beaconDistTemp[0]**2 - beaconDistTemp[1]**2 + beacons[1].x**2)/(2*beacons[1].x))
            y.append((beaconDistTemp[0]**2 - beaconDistTemp[2]**2 + beacons[2].x**2 + beacons[2].y**2 - (2*beacons[2].x*x[0]))/(2*beacons[2].y))
        elif measureType == '2': # 이등변삼각형 배열
            """
            x.append((beaconDistTemp[0]**2 - beaconDistTemp[1]**2 + beacons[1].x**2)/(2*beacons[1].x))
            y.append((beaconDistTemp[0]**2 - beaconDistTemp[2]**2 + beacons[2].x**2 + beacons[2].y**2 - (2*beacons[2].x*x[0]))/(2*beacons[2].y))
            """
            x.append((beaconDistTemp[2]**2 - beaconDistTemp[0]**2 - beacons[2].x**2)/(-2 * beacons[2].x))
            y.append((beaconDistTemp[2]**2 - beaconDistTemp[1]**2 + (2*beacons[2].x - 2*beacons[1].x)*x[0] - beacons[2].x**2 + beacons[1].x**2 - beacons[2].y**2)/(-2 * beacons[2].y))
        elif measureType == '3': # 직사각형 배열
            x.append((math.pow(beaconDistTemp[1], 2.0) - math.pow(beaconDistTemp[0],2.0) - math.pow(coord[0],2.0))/(-2 * coord[0]))
            y.append(math.sqrt(abs(beaconDistTemp[0]**2 - x[0]**2)))

            y.append((math.pow(beaconDistTemp[2], 2.0) - math.pow(beaconDistTemp[0],2.0) - math.pow(coord[1],2.0))/(-2 * coord[1]))
            x.append(math.sqrt(abs(beaconDistTemp[0]**2 - y[1]**2)))

            x.append((math.pow(beaconDistTemp[3], 2.0) - math.pow(beaconDistTemp[2],2.0) - math.pow(coord[0],2.0))/(-2 * coord[0]))
            y.append(math.sqrt(abs(beaconDistTemp[2]**2 - x[2]**2)) + coord[1])

            y.append((math.pow(beaconDistTemp[3], 2.0) - math.pow(beaconDistTemp[1],2.0) - math.pow(coord[1],2.0))/(-2 * coord[1]))
            x.append(math.sqrt(abs(beaconDistTemp[1]**2 - y[3]**2)) + coord[0])

        xcoord, ycoord = np.average(x), np.average(y)
        print('(' + str(j) + ', ' + str(i) + ')')
        print('측정:(' + str(round(xcoord, 3)) + ',' +  str(round(ycoord, 3)) + ')')
        print('오차:(' + str(round(abs(j-round(xcoord, 3)), 3)) + ',' + str(round(abs(i-round(ycoord, 3)), 3)) + ')')
        distErrorX.append(round(abs(j-round(xcoord, 3)), 3))
        distErrorY.append(round(abs(i-round(ycoord, 3)), 3))
printError(distErrorX, distErrorY, 'avg')