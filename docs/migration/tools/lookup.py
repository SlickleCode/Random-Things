import sys,re,os
V=os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.18.2/')
moj={}   # mojang class -> (obf, {fields: name->obf}, {methods: name->[(obf,sig)]})
cur=None
for l in open(V+'client_mappings.txt',encoding='utf8'):
    if l.startswith('#'): continue
    l=l.rstrip('\n')
    if not l.startswith(' '):
        m,o=l[:-1].split(' -> '); cur=(o,{},{}); moj[m]=cur
    else:
        e=re.sub(r'^\s*\d+:\d+:','',l).strip()
        if '(' in e:
            ret,rest=e.split(' ',1); name=rest[:rest.index('(')]; obf=rest.split(' -> ')[1]
            cur[2].setdefault(name,[]).append((obf,rest[rest.index('('):rest.index(')')+1],ret))
        else:
            t,rest=e.split(' ',1); name,obf=rest.split(' -> '); cur[1][name]=obf
srg={}  # obf class -> {('f',obf):id, ('m',obf,desc):id}
cur=None
for l in open(V+'mcp_mappings.tsrg',encoding='utf8'):
    if l.startswith('tsrg2'): continue
    l=l.rstrip('\n')
    if not l.startswith('\t'):
        p=l.split(' '); cur={}; srg[p[0]]=cur
    elif not l.startswith('\t\t'):
        p=l.strip().split(' ')
        if len(p)==3 and not p[1].startswith('('): cur[('f',p[0])]=p[1]
        elif len(p)==4: cur[('m',p[0],p[1])]=p[2]
PRIM={'int':'I','long':'J','float':'F','double':'D','boolean':'Z','byte':'B','short':'S','char':'C','void':'V'}
def ot(t):
    a=0
    while t.endswith('[]'): t=t[:-2]; a+=1
    return '['*a+(PRIM[t] if t in PRIM else 'L'+(moj[t][0] if t in moj else t).replace('.','/')+';')
def odesc(sig,ret):
    args=sig[1:-1]
    return '('+''.join(ot(x) for x in args.split(',') if x)+')'+ot(ret)
for q in sys.argv[1:]:
    c,n=q.rsplit('.',1)
    o,fs,ms=moj[c]
    if n in fs: print(q,'FIELD',srg[o].get(('f',fs[n])))
    for obf,sig,ret in ms.get(n,[]):
        ids=[v for k,v in srg[o].items() if k[0]=='m' and k[1]==obf and k[2]==odesc(sig,ret)]
        print(q,'METHOD',sig,ret,ids)
