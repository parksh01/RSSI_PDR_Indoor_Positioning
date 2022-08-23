import csv
import numpy as np
import matplotlib.pyplot as plt

xvalues = []
yvalues = []

xvalues_PDR = []
yvalues_PDR = []

# Read log files
path = input("5x5 : 1 / 지그재그 : 2 / 직선 : 3")
filepath = 'inputfile/'
if path == '1':
    filepath += '5x5정사각형/'
elif path == '2':
    filepath += '지그재그/'
elif path == '3':
    filepath += '직선왕복2회5미터/'

# Read log file of DR by accelerometer
f = open(filepath + 'CoordLog.csv', 'r')
rdr = csv.reader(f)
isFirstLine = True
for line in rdr:
    if isFirstLine:
        isFirstLine = False
    else:
        xvalues.append(float(line[0].strip(',')))
        yvalues.append(float(line[1].strip(',')))
f.close()
xvalues.pop(0)
yvalues.pop(0)

# Read log file of DR by step
# 7 steps are about 5 meter (1 step = 0.71m)
f = open(filepath + 'CoordLogPDR.csv', 'r')
rdr = csv.reader(f)
isFirstLine = True
for line in rdr:
    if isFirstLine:
        isFirstLine = False
    else:
        xvalues_PDR.append(float(line[0].strip(',')) * (5.0/7.0))
        yvalues_PDR.append(float(line[1].strip(',')) * (5.0/7.0))
f.close()
xvalues_PDR.pop(0)
yvalues_PDR.pop(0)

# Draw a path
fig, ax = plt.subplots()
ax.plot(xvalues, yvalues, color='blue', label='acc')
ax.plot(xvalues_PDR, yvalues_PDR, color='red', label='step')
ax.legend()
plt.show()