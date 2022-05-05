import matplotlib.pyplot as plt
import numpy as np
from scipy.optimize import curve_fit

front_or_back = input('front or back? : ')

# values of rssi, will be used for curve fitting.
rssi_y = []
rssi_avg = []

filename = 'inputdata'
if front_or_back == 'front':
    filename += '/front/'
elif front_or_back == 'back':
    filename += '/back/'

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
        rssi_avg.append(avg)


# Curve Fitting
def func(X, n, A): # a = n / b = A
    # distance to rssi
    return -10 * n * np.log10(X) + A
xvalues = np.arange(1, 7+1)
popt, pcov = curve_fit(func, xvalues, rssi_avg, method='lm')
print(popt)

# show the graph of measured rssi values and fitted curve
fig, ax = plt.subplots()
violin = ax.violinplot(rssi_y, positions=range(1, 7 + 1))
ax.set_xlabel('meters')
ax.set_ylabel('RSSI')
plt.plot(range(1, 7 + 1), rssi_avg, 'r^')
plt.plot(xvalues, func(xvalues, *popt))
plt.show()
