import numpy as np
import matplotlib.pyplot as plt

xvalues_aspect = []
yvalues_aspect = []

path = input("5x5 : 1 / 2.5m삼각형 : 2 / 직선 : 3 / 원 : 4")
if path == '1':
    xvalues_aspect = [0.0, 0.0, 5.0, 5.0, 0.0]
    yvalues_aspect = [0.0, 5.0, 5.0, 0.0, 0.0]
elif path == '2':
    xvalues_aspect = [0.0, 0.0, 3*np.cos(60*np.pi/180), 0.0]
    yvalues_aspect = [0.0, 3.0, 1.5, 0.0]
elif path == '3':
    xvalues_aspect = [0.0, 0.0, 0.0, 0.0]
    yvalues_aspect = [0.0, 5.0, 0.0, 5.0]
elif path == '4':
    for i in range(360):
        xvalues_aspect.append(1.5*np.cos(i*np.pi/180) + 1.5)
        yvalues_aspect.append(1.5*np.sin(i*np.pi/180))

# Draw a path
fig, ax = plt.subplots()
ax.plot(xvalues_aspect, yvalues_aspect, color='orange', label='aspected')
ax.legend()
plt.show()