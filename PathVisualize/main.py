import csv
import numpy as np
import matplotlib.pyplot as plt

xvalues = []
yvalues = []

xvalues_PDR = []
yvalues_PDR = []

xvalues_aspect = []
yvalues_aspect = []

# Read log files
path = input("5x5 : 1 / 2.5m삼각형 : 2 / 직선 : 3 / 원 : 4")
filepath = 'inputfile/'
if path == '1':
    filepath += '5m정사각형/'
    xvalues_aspect = [0.0, 0.0, 5.0, 5.0, 0.0]
    yvalues_aspect = [0.0, 5.0, 5.0, 0.0, 0.0]
elif path == '2':
    filepath += '2.5m삼각형/'
    xvalues_aspect = [0.0, 0.0, 3*np.cos(60*np.pi/180), 0.0]
    yvalues_aspect = [0.0, 3.0, 1.5, 0.0]
elif path == '3':
    filepath += '직선왕복2회5미터/'
    xvalues_aspect = [0.0, 0.0, 0.0, 0.0]
    yvalues_aspect = [0.0, 5.0, 0.0, 5.0]
elif path == '4':
    filepath += '원/'
    for i in range(360):
        xvalues_aspect.append(1.5*np.cos(i*np.pi/180) + 1.5)
        yvalues_aspect.append(1.5*np.sin(i*np.pi/180))

# Read log file of DR by accelerometer
f = open(filepath + 'CoordLog.csv', 'r')
rdr = csv.reader(f)
isFirstLine = True
for line in rdr:
    if isFirstLine:
        isFirstLine = False
    else:
        xvalues.append(-1*float(line[0].strip(',')))
        yvalues.append(float(line[1].strip(',')))
f.close()
xvalues.pop(0)
yvalues.pop(0)

# Read log file of DR by step
# 7 steps are about 5 meter (1 step = 0.71m)
f = open(filepath + 'CoordLogPDR.csv', 'r')
initx, inity = 0, 0
isInit = False
rdr = csv.reader(f)
isFirstLine = True
for line in rdr:
    if isFirstLine:
        isFirstLine = False
    else:
        if isInit == False:
            initx = float(line[0].strip(',')) * (5.0 / 7.0)
            inity = float(line[1].strip(',')) * (5.0 / 7.0)
            isInit = True
        xvalues_PDR.append(float(line[0].strip(',')) * (5.0/7.0) - initx)
        yvalues_PDR.append(float(line[1].strip(',')) * (5.0/7.0) - inity)
f.close()
xvalues_PDR.pop(0)
yvalues_PDR.pop(0)

# Draw a path
fig, ax = plt.subplots()
ax.plot(xvalues, yvalues, color='blue', label='acc')
ax.plot(xvalues_PDR, yvalues_PDR, color='red', label='step')
ax.plot(xvalues_aspect, yvalues_aspect, color='orange', label='aspected')
ax.legend()
plt.show()