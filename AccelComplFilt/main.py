import csv
import numpy as np
import matplotlib.pyplot as plt

# Read sensor data
f = open('inputData/sensordatatest.csv', 'r')
rdr = csv.reader(f)
xvalues = []
yvalues = []
zvalues = []
isFirstLine = True
for line in rdr:
    if isFirstLine:
        isFirstLine = False
    else:
        xvalues.append(float(line[0].strip(',')))
        yvalues.append(float(line[1].strip(',')))
        zvalues.append(float(line[2].strip(',')))
f.close()
xvalues.pop(0)
yvalues.pop(0)
zvalues.pop(0)
values = []
values.append(xvalues)
values.append(yvalues)
values.append(zvalues)

axis = 2

z_result = values[axis] # 측정값
est_result = [] # 추정값

# Step 1
z_diced = [] # S_i(n)
N = 9
for i in range(len(z_result) - N):
    temp = z_result[i:i+N]
    temp.reverse()
    z_diced.append(temp)

# Step 2
mu = np.average(range(1, N+1))
for i in range(len(z_diced)):
    m = np.average(z_diced[i])
    sigma = 0.0
    for j in range(N):
        sigma += (z_diced[i][j] - m)**2.0
    sigma /= N
    sigma = sigma**0.5

g_i = []
for j in range(len(z_diced)):
    g_i_temp = []
    for i in range(N):
        g_i_val = 1.0 / (sigma * np.sqrt(2.0*np.pi))
        g_i_val *= np.exp(-1.0 * ( ((i+1 - mu)**2.0)/(2.0*sigma*sigma) ))
        g_i_temp.append(g_i_val)
    g_i.append(g_i_temp)

R_i = []
for i in range(len(z_diced)):
    R_i_val = 0.0
    for n in range(N):
        R_i_val += z_diced[i][n] * g_i[i][n]
    R_i_val /= N
    R_i.append(R_i_val)

# Step 3
A_i = []
for i in range(len(z_diced)):
    A_i.append(np.average(z_diced[i]))

theta_i = []
for i in range(len(z_diced)):
    theta_i_val = 0.0
    for n in range(N):
        theta_i_val += z_diced[i][n]**2.0
    theta_i_val /= N
    theta_i_val -= A_i[i]
    theta_i.append(theta_i_val)

# Step 4
a = 0.1
O_i = []
for i in range(len(z_diced)):
    omega = 1 - (a ** 2.0 / theta_i[i])
    O_i.append(R_i[i] + omega*(z_diced[i][0] - R_i[i]))

z_result = values[axis][N:]
est_result = O_i

fig, ax = plt.subplots()
ax.plot(range(len(z_diced)), z_result, label='measure')
ax.plot(range(len(z_diced)), est_result, label='estimate')
ax.legend()
plt.show()