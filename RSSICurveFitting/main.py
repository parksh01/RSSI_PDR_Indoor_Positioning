import matplotlib.pyplot as plt
import numpy as np
from scipy.optimize import curve_fit

def most_frequent(data):
    return max(data, key=data.count)

front_or_back = input('front or back? : ')
kalman_or_not = input('kalman? (y/n) : ')

# values of rssi, will be used for curve fitting.
rssi_y = []
rssi_avg = []
rssi_median = []
rssi_max = []

filename = 'inputdata'
if front_or_back == 'front':
    filename += '/front/'
elif front_or_back == 'back':
    filename += '/back/'

if kalman_or_not == 'y':
    filename += 'kf/'

# read files and get rssi values
for i in range(7):
    with open(filename + str(i + 1) + 'm ' + front_or_back + '.csv', 'r') as f:
        print('Processing : ' + filename + str(i + 1) + 'm ' + front_or_back + '.csv')
        line = None
        avg = 0
        tick = 0
        rssiCurrentValues = []
        for j in range(100):
            line = f.readline().strip()
            rssiCurrentValues.append(float(line))
            avg += float(line)
            tick += 1
        avg /= float(tick)
        rssi_y.append(rssiCurrentValues)
        rssi_median.append(np.median(rssiCurrentValues))
        rssi_max.append(most_frequent(rssiCurrentValues))
        rssi_avg.append(avg)


# Curve Fitting
def func(X, n, A):
    # distance to rssi
    return -10 * n * np.log10(X) + A
xvalues = np.arange(1, 7+1)
popt1, pcov1 = curve_fit(func, xvalues, rssi_avg, method='lm')
popt2, pcov2 = curve_fit(func, xvalues, rssi_median, method='lm')
popt3, pcov3 = curve_fit(func, xvalues, rssi_max, method='lm')

print('A =', rssi_avg[0])
print('avg :', popt1)
print('median :', popt2)
print('most :', popt3)

# show the graph of measured rssi values and fitted curve
fig, ax = plt.subplots()
violin = ax.violinplot(rssi_y, positions=range(1, 7 + 1))
ax.set_xlabel('meters')
ax.set_ylabel('RSSI')
plt.plot(range(1, 7 + 1), rssi_avg, 'r^', label='rssi_avg')
plt.plot(range(1, 7 + 1), rssi_median, 'bo', label='rssi_median')
plt.plot(range(1, 7 + 1), rssi_max, 'gs', label='rssi_most')
plt.plot(xvalues, func(xvalues, *popt1), label='curve(avg)')
plt.plot(xvalues, func(xvalues, *popt2), label='curve(median)')
plt.plot(xvalues, func(xvalues, *popt3), label='curve(most)')
plt.legend()
plt.show()
