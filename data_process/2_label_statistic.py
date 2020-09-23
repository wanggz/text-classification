#coding:utf-8
import json

labels = {}

for i in range(1,16):
    fileName = "news_sohusite_"+str(i)+".json"
    print(fileName)
    file = open("../data/"+fileName)
    while 1:
        line = file.readline()
        if not line:
            break
        doc = json.loads(line)
        label = doc['url'].split('com',2)[0][7:]+'com'
        if label in labels.keys():
            labels[label] = labels[label]+1
        else:
            labels[label] = 1
    file.close()

print(labels)