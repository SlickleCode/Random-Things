import collections,sys
t=open(sys.argv[1],encoding='utf-8',errors='replace').read().split('\n')
c=collections.Counter();m=collections.Counter()
for i,l in enumerate(t):
    if ': error: ' in l:
        msg=l.split(': error: ',1)[1]
        if msg.startswith('cannot find symbol'):
            sym=next((t[k].strip() for k in range(i+1,min(i+6,len(t))) if 'symbol:' in t[k]),'')
            c[sym]+=1
        else: m[msg[:110]]+=1
print('--- symbols'); [print(n,s) for s,n in c.most_common(int(sys.argv[2]) if len(sys.argv)>2 else 40)]
print('--- other'); [print(n,s) for s,n in m.most_common(25)]
