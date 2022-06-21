# RSSILocation Refactored
import numpy as np

def printError(distErrorX, distErrorY, keyword):
    print('error(' + keyword + ') : ' + str(np.average(distErrorX)) + ',' + str(np.average(distErrorY)))
    print('std(' + keyword + ') : ' + str(np.std(distErrorX)) + ',' + str(np.std(distErrorY)))

def errorRatio(x1, x2, xerror1, xerror2):
    return (x1*xerror2 + x2*xerror1)/(xerror1+xerror2)

# pre defined variables area
foldername = 'Measure20220618'
filename = 'Coord'
distLog = 'Distance Log'
coord = 5

# start of algorithm
distErrorX0 = []
distErrorY0 = []
distErrorX1 = []
distErrorY1 = []
distErrorX2 = []
distErrorY2 = []
# beacon01 : (5, 0)
# beacon02 : (0, 0)
# beacon03 : (0, 5)
for i in range(1, coord):
    for j in range(1, coord):
        f = open(foldername + '/' + filename + str(j) + str(i) + '/' + distLog + '.csv', 'r')
        beaconDist1 = []
        beaconDist2 = []
        beaconDist3 = []
        x, y = [], []
        while True:
            line = f.readline().strip()
            processLine = line.split(',')
            if not line:
                break
            elif processLine[0].isdigit() and processLine[2] != 'null' and processLine[3] != 'null' and processLine[4] != 'null':
                beaconDist1.append(float(processLine[2]))
                beaconDist2.append(float(processLine[3]))
                beaconDist3.append(float(processLine[4]))
        x.append(((np.average(beaconDist1) ** 2) - (np.average(beaconDist2) ** 2) - (float(coord)**2)) / (-2 * float(coord)))
        y.append(((np.average(beaconDist3) ** 2) - (np.average(beaconDist2) ** 2) - (float(coord)**2)) / (-2 * float(coord)))
        x.append(abs(coord - (abs((np.average(beaconDist3)**2)-(x[0]**2))**0.5)))
        y.append(abs(coord - (abs((np.average(beaconDist1)**2)-(y[0]**2))**0.5)))
        print('(' + str(j) + ', ' + str(i) + ') : ' + str(round(np.average(x), 3)) + ',' +  str(round(np.average(y), 3)) + '/ error : ' + str(round(abs(j-round(np.average(x), 3)), 3)) + ',' + str(round(abs(i-round(np.average(y), 3)), 3)))
        distErrorX0.append(round(abs(j-round(np.average(x), 3)), 3))
        distErrorY0.append(round(abs(i-round(np.average(y), 3)), 3))
                
printError(distErrorX0, distErrorY0, 'avg')