import csvopen
import random
import math
import matplotlib.pyplot as plt
import numpy as np

def printError(distErrorX, distErrorY, keyword):
    print('평균 오차 : (' + str(round(np.average(distErrorX), 3)) + ',' + str(round(np.average(distErrorY), 3)) + ') / ' + '표준편차 : (' + str(round(np.std(distErrorX), 3)) + ',' + str(round(np.std(distErrorY), 3)) + ')')

def get_average(data):
    sum, index = 0, 0
    for i in data:
        sum += i
        index += 1
    return float(sum)/float(index)

def get_median(data):
    data = sorted(data)
    centerIndex = len(data) // 2
    return (data[centerIndex] + data[-centerIndex - 1]) / 2

def get_normalpdf(particle_coord, measured_data):
    sigma = 0.5
    val = 1 / (sigma * math.sqrt(2 * math.pi))
    val *= math.exp(-1 * (particle_coord - measured_data)**2 / (2 * sigma**2))
    return val

def pf(measured, particleCount, measureRange, moveRange):
    # particle : [x, y, weight]
    particle = []
    result = []
    # Generate particles as many as measureRange
    for i in range(particleCount):
        particle.append([random.random()*measureRange, random.random()*measureRange, 0.0])

    for coord in measured:
        # Prediction
        for i in range(particleCount):
            particle[i][0] += (random.random()*moveRange - moveRange/2.0)
            particle[i][1] += (random.random()*moveRange - moveRange/2.0)
            particle[i][2] = 0.0

        # Update
        # The particle closer to measured coordinate gets higher weight
        for i in range(particleCount):
            particle[i][2] += (1 / math.sqrt((particle[i][0] - coord[0])**2 + (particle[i][1] - coord[1])**2))

        # Resampling
        w = []
        for i in particle:
            w.append(i[2])

        # Discard particles having small weight (smaller than median)
        w_median = get_median(w)
        for i in range(particleCount):
            if w[i] < w_median:
                w[i] = 0.0

        # Random select particles by their weights
        particle_num = (random.choices(range(particleCount), weights=w, k=particleCount))
        result.append(particle[max(particle_num, key=particle_num.count)])

        # And redistribute the particles.
        newParticle = []
        for i in particle_num:
            newParticle.append([particle[i][0], particle[i][1], 0.0])
        particle = newParticle[:]

    return result

distErrorX, distErrorY = [], []
filtCoordX, filtCoordY = [], []

for i in range(1, 5):
    for j in range(1, 5):
        testcasePath = 'testcase/c' + str(j) + str(i) + '.csv'
        val = csvopen.readcsv(testcasePath)
        val = csvopen.locateCoordinate(val, 5)
        pfvalue = pf(val, 1000, 5, 0.2)

        x = []
        y = []
        for n in pfvalue:
            x.append(n[0])
            y.append(n[1])

        x_avg, y_avg = get_average(x), get_average(y)

        print('current position : ' + str(j) + ',' + str(i))
        print('측정:(' + str(round(x_avg, 3)) + ',' + str(round(y_avg, 3)) + ')')
        print('오차:(' + str(round(abs(x_avg - j), 3)) + ',' + str(round(abs(y_avg - j), 3)) + ')')
        filtCoordX.append(x_avg)
        filtCoordY.append(y_avg)
        distErrorX.append(abs(x_avg - j))
        distErrorY.append(abs(y_avg - i))
        """
        plt.scatter(x, y, s=1, label='particles')
        plt.scatter(j, i, s=5, label='expected')
        plt.scatter(x_avg, y_avg, s=10, label='filtered')
        plt.legend()
        plt.show()
        """
printError(distErrorX, distErrorY, 'avg')

plt.scatter(filtCoordX, filtCoordY, s=5, label='filtered')
plt.scatter([1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4], [1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4], s=5, label='expected')
plt.legend()
plt.show()


