import re,collections,sys
t=open(sys.argv[1],encoding='utf-8',errors='replace').read().split('\n')
c=collections.Counter()
for l in t:
    m=re.match(r'^(.+\.java):\d+: error: (.*)$',l)
    if m: c[re.split(r'randomthings[\/]',m.group(1))[-1]]+=1
print(sum(c.values()))
for f,n in c.most_common(int(sys.argv[2]) if len(sys.argv)>2 else 60): print(n,f)
