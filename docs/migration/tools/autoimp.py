import re,sys,pickle,collections
classes,_,_=pickle.load(open('maps.pkl','rb'))
by=collections.defaultdict(set)
for m in classes.values():
    if '$' in m: continue
    by[m.split('.')[-1]].add(m)
err=open(sys.argv[1],encoding='utf8',errors='replace').read().split('\n')
need=collections.defaultdict(set)
for i,l in enumerate(err):
    m=re.match(r'(.*\.java):\d+: error: cannot find symbol',l)
    if m and i+3<len(err):
        for j in range(i+1,min(i+5,len(err))):
            mm=re.match(r'\s+symbol:\s+(?:class|variable) ([A-Z]\w+)',err[j])
            if mm: need[m.group(1)].add(mm.group(1)); break
amb=set()
for f,names in need.items():
    s=open(f,encoding='utf8').read()
    add=[]
    for n in sorted(names):
        c=by.get(n,set())
        if len(c)==1: add.append(next(iter(c)))
        elif len(c)>1:
            amb.add((n,tuple(sorted(c))))
    add=[a for a in add if f'import {a};' not in s]
    if not add: continue
    block=''.join(f'import {a};\n' for a in add)
    s=re.sub(r'(package [^;]+;\r?\n)',lambda m:m.group(1)+'\n'+block,s,count=1)
    open(f,'w',encoding='utf8',newline='').write(s)
print('files',len(need)); 
for a in sorted(amb): print('AMBIG',a)
