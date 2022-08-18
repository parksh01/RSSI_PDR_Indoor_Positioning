import csv
import numpy as np
import matplotlib.pyplot as plt

# Read sensor data
f = open('inputData/Sensor Data - 2022-08-16-12시 52분 16초.csv', 'r')
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

axis = 0
x = values[axis][0] # 초기 추정값
P = np.array([[1]]) # 초기 추정값에 대한 오차 공분산
A = np.array([[1]]) # 시스템 행렬
H = np.array([[1]]) # 출력 행렬
Q = np.array([[1]]) # 시스템 오차
R = np.array([[10]]) # 측정 오차

z = values[axis]
def SimpleKalman(z):
    global x, P, A, H, Q, R
    # 추값과 오차 공분산 예측
    xp = A.dot(x)
    Pp = A.dot(P).dot(A.T) + Q

    # 칼만 이득 계산
    K = Pp.dot(H.T).dot(np.linalg.inv(H.dot(P).dot(H.T) + R))

    # 추정값 계산
    x = xp + K.dot(z - H.dot(xp))

    # 오차 공분산 계산
    P = Pp - K.dot(H).dot(Pp)

    return x, P, K

z_result = [] # 측정값
est_result = [] # 추정값
Cov_result = []
Kg_result = []

for i in range(len(values[axis])):
    z = values[axis][i]
    est, Cov, Kg = SimpleKalman(z)
    z_result.append(z)
    est_result.append(est[0])
    Cov_result.append(Cov[0, 0])
    Kg_result.append(Kg[0, 0])

vel_z = []
vel_z_value = 0.0
for i in z_result:
    vel_z_value = vel_z_value + i
    vel_z.append(vel_z_value)

vel_est = []
vel_est_value = 0.0
for i in est_result:
    vel_est_value = vel_est_value + i
    vel_est.append(vel_est_value)

fig, ax = plt.subplots()

ax.plot(range(len(values[0])), z_result, label='measure')
ax.plot(range(len(values[0])), est_result, label='estimate')
ax.plot(range(len(values[0])), vel_z, label='vel_z')
ax.plot(range(len(values[0])), vel_est, label='vel_Est')

ax.legend()
plt.show()