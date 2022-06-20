# RSSILocation Refactored
import numpy as np

# pre defined variables area
foldername = 'Measure20220618'
filename = 'Coord'
coord = 5
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
# beacon01 : (5, 0)
# beacon02 : (0, 0)
# beacon03 : (0, 5)
for i in range(1, coord):
    for j in range(1, coord):
        # processing coordinate (j, i)
        beaconDistTemp = []
        beaconNum = -1
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
                    beaconRSSI.append(float(processLine[1]))
                    if beaconNum == 2:
                        beaconDistTemp.append(round(10 ** ((float(processLine[1]) - beacon_A_back[beaconNum])/(-10 * beacon_n_back[beaconNum])), 3))
                    else:
                        beaconDistTemp.append(round(10 ** ((float(processLine[1]) - beacon_A_front[beaconNum])/(-10 * beacon_n_front[beaconNum])), 3))
                    break
        beaconDist.append(beaconDistTemp)
        xcoord1 = ((beaconDistTemp[0] ** 2) - (beaconDistTemp[1] ** 2) - (float(coord)**2)) / (-2 * float(coord))
        ycoord1 = ((beaconDistTemp[2] ** 2) - (beaconDistTemp[1] ** 2) - (float(coord)**2)) / (-2 * float(coord))
        xcoord2 = abs(coord - (abs((beaconDistTemp[2]**2)-(xcoord1**2))**0.5))
        ycoord2 = abs(coord - (abs((beaconDistTemp[0]**2)-(ycoord1**2))**0.5))
        xcoord, ycoord = (xcoord1+xcoord2)/2, (ycoord1+ycoord2)/2
        print('(' + str(j) + ', ' + str(i) + ') : ' + str(round(xcoord, 3)) + ',' +  str(round(ycoord, 3)) + '/ error : ' + str(round(abs(j-round(xcoord, 3)), 3)) + ',' + str(round(abs(i-round(ycoord, 3)), 3)))
        distErrorX.append(round(abs(j-round(xcoord, 3)), 3))
        distErrorY.append(round(abs(i-round(ycoord, 3)), 3))
print('error : ' + str(np.average(distErrorX)) + ',' + str(np.average(distErrorY)))
print('std : ' + str(np.std(distErrorX)) + ',' + str(np.std(distErrorY)))