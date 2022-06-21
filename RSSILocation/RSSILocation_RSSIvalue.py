# RSSILocation Refactored
import numpy as np

def printError(distErrorX, distErrorY, keyword):
    print('error(' + keyword + ') : ' + str(np.average(distErrorX)) + ',' + str(np.average(distErrorY)))
    print('std(' + keyword + ') : ' + str(np.std(distErrorX)) + ',' + str(np.std(distErrorY)))

def errorRatio(x1, x2, xerror1, xerror2):
    return (x1*xerror2 + x2*xerror1)/(xerror1+xerror2)

# pre defined variables area
foldername = 'Measure20220518'
filename = 'Coord'
coord = 4
beacons = ['A81B6AAE5FF6', 'A81B6AAE4C6B', 'A81B6AAE5260']
beacon_A_front = [-64.44, -59.717, -58.582]
beacon_n_front = [1.569, 1.769, 1.837]
beacon_A_back = [-66.028, -61.764, -60.356]
beacon_n_back = [1.071, 0.967, 1.709]

# start of algorithm
beaconRSSI = []
beaconDist = []
distErrorX = []
distErrorY = []
distErrorX1 = []
distErrorY1 = []
distErrorX2 = []
distErrorY2 = []
# beacon01 : (0, 0)
# beacon02 : (5, 0)
# beacon03 : (0, 5)
for i in range(1, coord):
    for j in range(1, coord):
        # processing coordinate (j, i)
        beaconDistTemp = []
        beaconNum = -1
        x, y = [], []
        for beaconInfo in beacons:
            beaconNum += 1
            f = open(foldername + '/' + filename + str(j) + str(i) + '/' + beaconInfo + '.csv', 'r')
            while True:
                line = f.readline()
                processLine = line.split(',')
                if not line:
                    break
                elif processLine[0] == 'avg(rssi)':
                    line = f.readline()
                    processLine = line.split(',')
                    # use Kalman filtered values
                    beaconRSSI.append(float(processLine[1]))
                    if beaconNum == 2:
                        beaconDistTemp.append(round(10 ** ((float(processLine[1]) - beacon_A_back[beaconNum])/(-10 * beacon_n_back[beaconNum])), 3))
                    else:
                        beaconDistTemp.append(round(10 ** ((float(processLine[1]) - beacon_A_front[beaconNum])/(-10 * beacon_n_front[beaconNum])), 3))
                    break
        beaconDist.append(beaconDistTemp)
        """
        x.append(((beaconDistTemp[0] ** 2) - (beaconDistTemp[1] ** 2) - (float(coord)**2)) / (-2 * float(coord)))
        y.append(((beaconDistTemp[2] ** 2) - (beaconDistTemp[1] ** 2) - (float(coord)**2)) / (-2 * float(coord)))
        x.append(abs(coord - (abs((beaconDistTemp[2]**2)-(x[0]**2))**0.5)))
        y.append(abs(coord - (abs((beaconDistTemp[0]**2)-(y[0]**2))**0.5)))
        """
        x.append(((beaconDistTemp[1] ** 2) - (beaconDistTemp[0] ** 2) - (float(coord)**2)) / (-2 * float(coord)))
        y.append(((beaconDistTemp[2] ** 2) - (beaconDistTemp[0] ** 2) - (float(coord)**2)) / (-2 * float(coord)))
        x.append(abs(coord - (abs((beaconDistTemp[2]**2)-(x[0]**2))**0.5)))
        y.append(abs(coord - (abs((beaconDistTemp[1]**2)-(y[0]**2))**0.5)))
        
        xcoord, ycoord = np.average(x), np.average(y)
        print('(' + str(j) + ', ' + str(i) + ') : ' + str(round(xcoord, 3)) + ',' +  str(round(ycoord, 3)) + '/ error : ' + str(round(abs(j-round(xcoord, 3)), 3)) + ',' + str(round(abs(i-round(ycoord, 3)), 3)))
        distErrorX.append(round(abs(j-round(xcoord, 3)), 3))
        distErrorY.append(round(abs(i-round(ycoord, 3)), 3))
        distErrorX1.append(round(abs(j-round(x[0], 3)), 3))
        distErrorY1.append(round(abs(i-round(y[0], 3)), 3))
        distErrorX2.append(round(abs(j-round(x[1], 3)), 3))
        distErrorY2.append(round(abs(i-round(y[1], 3)), 3))
printError(distErrorX, distErrorY, 'avg')
printError(distErrorX1, distErrorY1, '1')
printError(distErrorX2, distErrorY2, '2')