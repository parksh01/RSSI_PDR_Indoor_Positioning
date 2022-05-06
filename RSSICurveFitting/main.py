import matplotlib.pyplot as plt
import numpy as np
from scipy.optimize import curve_fit

def most_frequent(data):
    return max(data, key=data.count)

# values of rssi, will be used for curve fitting.
rssi_y_raw = []
rssi_y_kf = []

rssi_avg_raw = []
rssi_avg_kf = []

front_or_back = input("front or back? : ")
meters = int(input("meters? : "))
kf_or_raw = input("kf or raw? : ")

filename = 'inputdata2/'

# read files and get rssi values
for i in range(7):
    with open(filename + str(i + 1) + ' ' + front_or_back + '.csv', 'r') as f:
        print('Processing : ' + filename + str(i + 1) + 'm ' + front_or_back + '.csv')
        line = None
        avg = 0
        tick = 0
        rssiCurrentValuesRaw = []
        rssiCurrentValuesKF = []
        j = 0
        while True:
            line = f.readline().strip()
            if line == '':
                break
            rssiCurrentValuesRaw.append(float(line.split(',')[0]))
            rssiCurrentValuesKF.append(float(line.split(',')[1]))
            tick += 1
        rssi_y_raw.append(rssiCurrentValuesRaw)
        rssi_y_kf.append(rssiCurrentValuesKF)
        rssi_avg_raw.append(round(np.average(rssiCurrentValuesRaw), 3))
        rssi_avg_kf.append(round(np.average(rssiCurrentValuesKF), 3))

# Curve Fitting
def func(X, n, A):
    # distance to rssi
    return -10 * n * np.log10(X) + A

xvalues = np.arange(1, meters + 1)
popt1, pcov1 = curve_fit(func, xvalues, rssi_avg_raw[:meters], method='lm')
popt2, pcov2 = curve_fit(func, xvalues, rssi_avg_kf[:meters], method='lm')

print('raw :', popt1)
print('kf :', popt2)
print('avg_raw :', rssi_avg_raw)
print('avg_kf :', rssi_avg_kf)

rssi_to_dist_raw = []
rssi_to_dist_raw_error = []
for i in range(7):
      rssi_to_dist_raw.append(round(10 ** ((popt1[1] - rssi_avg_raw[i])/(10*popt1[0])), 3))
      rssi_to_dist_raw_error.append(np.absolute(round(float(i + 1) - rssi_to_dist_raw[i], 3)))
print('dist(raw) :', rssi_to_dist_raw)
print('error(raw) :', rssi_to_dist_raw_error)
print('mean error(raw) :', np.mean(rssi_to_dist_raw_error))
print('error std(raw) :', np.std(rssi_to_dist_raw_error))
print()

rssi_to_dist_kf = []
rssi_to_dist_kf_error = []
for i in range(7):
      rssi_to_dist_kf.append(round(10 ** ((popt2[1] - rssi_avg_kf[i])/(10*popt2[0])), 3))
      rssi_to_dist_kf_error.append(np.absolute(round(float(i + 1) - rssi_to_dist_kf[i], 3)))
print('dist(kf) :', rssi_to_dist_kf)
print('error(kf) :', rssi_to_dist_kf_error)
print('mean error(kf) :', np.mean(rssi_to_dist_kf_error))
print('error std(kf) :', np.std(rssi_to_dist_kf_error))

# show the graph of measured rssi values and fitted curve
fig, ax = plt.subplots()
ax.set_xlabel('meters')
ax.set_ylabel('RSSI')

if kf_or_raw == 'kf':
    violin = ax.violinplot(rssi_y_kf, positions=range(1, 7 + 1))
    plt.plot(range(1, 7 + 1), rssi_avg_kf, 'r^', label='rssi_avg_kf')
    plt.plot(range(1, 7 + 1), func(range(1, 7 + 1), *popt2), label='curve(avg)', color='r')
elif kf_or_raw == 'raw':
    violin = ax.violinplot(rssi_y_raw, positions=range(1, 7 + 1))
    plt.plot(range(1, 7 + 1), rssi_avg_raw, 'r^', label='rssi_avg_raw')
    plt.plot(range(1, 7 + 1), func(range(1, 7 + 1), *popt1), label='curve(avg)', color='r')

plt.legend()
plt.show()
