import csv
from math import sqrt, fabs

def readcsv(path):
    f = open(path, 'r')
    rdr = csv.reader(f)
    values = []
    linecount = 0
    for line in rdr:
        if linecount == 0:
            linecount += 1
        elif (line[2] != 'null') and (line[3] != 'null') and (line[4] != 'null') and (linecount != 0):
            values.append([float(line[2]), float(line[3]), float(line[4])])
    f.close()
    return values

def locateCoordinate(values, c):
    coord = []
    for r in values:
        x1 = (r[1]**2 - r[0]**2 - c**2) / (-2 * c)
        y1 = c - sqrt(fabs(r[2]**2 - x1**2))
        y2 = (r[2]**2 - r[0]**2 - c**2) / (-2 * c)
        x2 = c - sqrt(fabs(r[1]**2 - y2**2))
        coord.append([(x1+x2)/2, (y1+y2)/2])
    return coord