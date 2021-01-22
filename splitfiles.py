with open("/Users/plumbus/School/code2vec/vectors.log", "r") as f:
    i = 1
    for line in f:
        if line.strip() == "":
            continue
        output = open('./GoodBlockVec/%d.vectors' % i,'w')
        output.write(line)
        output.close()
        i+=1

