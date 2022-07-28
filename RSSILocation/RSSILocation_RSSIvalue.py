# RSSILocation Refactored
import numpy as np

class Beacon:
    def __init__(self, addr, A_front, n_front, A_back, n_back, isBack, x, y):
        self.addr = addr
        self.A_front = A_front
        self.n_front = n_front
        self.A_back = A_back
        self.n_back = n_back
        self.isBack = isBack
        self.x = x
        self.y = y
    def getDist(self, rssi):
        if not self.isBack:
            return 10**((rssi - self.A_front)/(-10 * self.n_front))
        elif self.isBack:
            return 10**((rssi - self.A_back)/(-10 * self.n_back))

def printError(distErrorX, distErrorY, keyword):
    print('error(' + keyword + ') : ' + str(np.average(distErrorX)) + ',' + str(np.average(distErrorY)))
    print('std(' + keyword + ') : ' + str(np.std(distErrorX)) + ',' + str(np.std(distErrorY)))

def errorRatio(x1, x2, xerror1, xerror2):
    return (x1*xerror2 + x2*xerror1)/(xerror1+xerror2)

# pre defined variables (beacon info and log file locations)
foldername = 'Measure20220518'
filename = 'Coord'
beacons = []
beacons.append(Beacon('A81B6AAE5FF6', -64.44, 1.569, -66.028, 1.071, False, x = 0.0, y = 0.0))
beacons.append(Beacon('A81B6AAE4C6B', -59.717, 1.769, -61.764, 0.967, False, x = 4.0, y = 0.0))
beacons.append(Beacon('A81B6AAE5260', -58.582, 1.837, -60.356, 1.709, True, x = 0.0, y = 4.0))
coord = [beacons[1].x, beacons[2].y]

# start of algorithm
measureType = str()
if len(beacons) == 3:
    measureType = input("type? 1 / 2 : ")
beaconRSSI = []
beaconDist = []
distErrorX = []
distErrorY = []
for i in range(1, int(coord[1])):
    for j in range(1, int(coord[0])):
        # processing coordinate (j, i)
        beaconDistTemp = []
        beaconNum = -1
        x, y = [], []

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
                    beaconRSSI.append(float(processLine[1]))
                    beaconDistTemp.append(beaconInfo.getDist(float(processLine[1])))
                    break
        beaconDist.append(beaconDistTemp)

        # then calculate the coordinate.
        if len(beacons) == 3:
            if measureType == '1':
                x.append(((beaconDistTemp[1] ** 2) - (beaconDistTemp[0] ** 2) - (float(coord[0]) ** 2)) / (-2 * float(coord[0])))
                y.append(((beaconDistTemp[2] ** 2) - (beaconDistTemp[0] ** 2) - (float(coord[1]) ** 2)) / (-2 * float(coord[1])))
                x.append(abs((beaconDistTemp[0] ** 2 - y[0] ** 2) ** 0.5))
                y.append(abs((beaconDistTemp[0] ** 2 - x[0] ** 2) ** 0.5))
            elif measureType == '2':
                x.append(((beaconDistTemp[1] ** 2) - (beaconDistTemp[0] ** 2) - (float(coord[0]) ** 2)) / (-2 * float(coord[0])))
                y.append(((beaconDistTemp[2] ** 2) - (beaconDistTemp[0] ** 2) - (float(coord[1]) ** 2)) / (-2 * float(coord[1])))
                x.append(abs(coord[0] - (abs((beaconDistTemp[1] ** 2) - (y[0] ** 2)) ** 0.5)))
                y.append(abs(coord[1] - (abs((beaconDistTemp[2] ** 2) - (x[0] ** 2)) ** 0.5)))
        elif len(beacons) == 4:
            x.append((beaconDistTemp[1]**2 - beaconDistTemp[0]**2 - float(coord[0])**2)/(-2 * float(coord[0])))
            y.append(abs(beaconDistTemp[0]**2 - x[0]**2)**0.5)

            y.append((beaconDistTemp[2]**2 - beaconDistTemp[0]**2 - float(coord[1])**2)/(-2 * float(coord[1])))
            x.append(abs(beaconDistTemp[0]**2 - y[1]**2)**0.5)

            x.append((beaconDistTemp[3]**2 - beaconDistTemp[2]**2 - float(coord[0])**2)/(-2 * float(coord[0])))
            y.append(abs(beaconDistTemp[2]**2 - x[2]**2)**0.5 + float(coord[1]))

            y.append((beaconDistTemp[3]**2 - beaconDistTemp[1]**2 - float(coord[1]))/(-2 * float(coord[1])))
            x.append(abs(beaconDistTemp[1]**2 - y[3]**2)**0.5 + float(coord[0]))
        
        xcoord, ycoord = np.average(x), np.average(y)
        print('(' + str(j) + ', ' + str(i) + ') : ' + str(round(xcoord, 3)) + ',' +  str(round(ycoord, 3)) + '/ error : ' + str(round(abs(j-round(xcoord, 3)), 3)) + ',' + str(round(abs(i-round(ycoord, 3)), 3)))
        distErrorX.append(round(abs(j-round(xcoord, 3)), 3))
        distErrorY.append(round(abs(i-round(ycoord, 3)), 3))
printError(distErrorX, distErrorY, 'avg')