import csvopen
import random
import math

def get_median(data):
    data = sorted(data)
    centerIndex = len(data) // 2
    return (data[centerIndex] + data[-centerIndex - 1]) / 2

def get_normalpdf(particle_coord, measured_data):
    sigma = 0.5
    val = 1 / (sigma * math.sqrt(2 * math.pi))
    val *= math.exp(-1 * (particle_coord - measured_data)**2 / (2 * sigma**2))
    return val

def pf(measured, particleCount, measureRange):
    # particle : [x, y, weight]
    particle = []
    result = []
    # Generate particles as many as measureRange
    for i in range(particleCount):
        particle.append([random.random()*measureRange, random.random()*measureRange, 0.0])

    for coord in measured:
        # Prediction
        for i in range(particleCount):
            particle[i][0] += (random.random()*0.3 - 0.15)
            particle[i][1] += (random.random()*0.3 - 0.15)
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



val = csvopen.readcsv('testcase/Distance Log 2,2.csv')
val = csvopen.locateCoordinate(val, 5)
pfvalue = pf(val, 500, 5)
x = 0.0
y = 0.0
for i in range(len(val)):
    print(str(pfvalue[i][0]) + ',' + str(pfvalue[i][1]) + ',' + str(val[i][0]) + ',' + str(val[i][1]))