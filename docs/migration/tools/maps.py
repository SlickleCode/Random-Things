import re, csv, json, sys, pickle, os
G=os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.16.5/mcp_mappings.tsrg')
S=os.path.dirname(os.path.abspath(__file__))
PRIM={'int':'I','long':'J','float':'F','double':'D','boolean':'Z','byte':'B','short':'S','char':'C','void':'V'}
# tsrg: obf class -> srg class; members
cls_obf2srg={}; members={}  # obfclass -> {('f',obf):srg, ('m',obf,desc):srg}
cur=None
for line in open(G,encoding='utf8'):
    line=line.rstrip('\n')
    if not line.startswith('\t'):
        o,s=line.split(' '); cls_obf2srg[o]=s; cur=o; members[cur]={}
    else:
        p=line.strip().split(' ')
        if len(p)==2: members[cur][('f',p[0])]=p[1]
        else: members[cur][('m',p[0],p[1])]=p[2]
# mojang 1.16.5
moj2obf={}; obf2moj={}; mojmembers={}
cur=None
for line in open(S+'/client16.txt',encoding='utf8'):
    if line.startswith('#'): continue
    line=line.rstrip('\n')
    if not line.startswith(' '):
        m,o=line[:-1].split(' -> '); moj2obf[m]=o; obf2moj[o]=m; cur=m; mojmembers[cur]=[]
    else:
        mojmembers[cur].append(line.strip())
def obftype(t):
    arr=0
    while t.endswith('[]'): t=t[:-2]; arr+=1
    if t in PRIM: r=PRIM[t]
    else: r='L'+moj2obf.get(t,t).replace('.','/')+';'
    return '['*arr+r
mcp_m={}; 
for r in csv.DictReader(open(S+'/mcp/methods.csv',encoding='utf8')): mcp_m[r['searge']]=r['name']
mcp_f={}
for r in csv.DictReader(open(S+'/mcp/fields.csv',encoding='utf8')): mcp_f[r['searge']]=r['name']
# result: methods[(mcpClassFQN, mcpName)] = (mojClassFQN, mojName) ; classes mcp fqn -> moj fqn
classes={}; methods={}; fields={}
obf2srgcls={o:s.replace('/','.') for o,s in cls_obf2srg.items()}
for moj,obf in moj2obf.items():
    if obf not in cls_obf2srg: continue
    srgc=obf2srgcls[obf]
    classes[srgc]=moj
    mem=members[obf]
    for e in mojmembers[moj]:
        if '(' in e:
            e2=re.sub(r'^\d+:\d+:','',e); ret_name,rest=e2.split(' ',1)
            name=rest[:rest.index('(')]; args=rest[rest.index('(')+1:rest.index(')')]
            mobf=rest.split(' -> ')[1]
            if name=='<init>' or name=='<clinit>': continue
            ad=''.join(obftype(a) for a in args.split(',') if a)
            desc='('+ad+')'+obftype(ret_name)
            srg=mem.get(('m',mobf,desc))
            if srg is None: continue
            mcp=mcp_m.get(srg,srg)
            methods.setdefault((srgc,mcp),(moj,name))
        else:
            t,rest=e.split(' ',1); name,mobf=rest.split(' -> ')
            srg=mem.get(('f',mobf))
            if srg is None: continue
            mcp=mcp_f.get(srg,srg)
            fields.setdefault((srgc,mcp),(moj,name))
pickle.dump((classes,methods,fields),open(S+'/maps.pkl','wb'))
print(len(classes),len(methods),len(fields))
with open(S+'/classes.tsv','w',encoding='utf8') as f:
    for k,v in classes.items(): f.write(f'{k}\t{v}\n')
with open(S+'/methods.tsv','w',encoding='utf8') as f:
    for (c,n),(mc,mn) in methods.items(): f.write(f'{c}\t{n}\t{mn}\n')
with open(S+'/fields.tsv','w',encoding='utf8') as f:
    for (c,n),(mc,mn) in fields.items(): f.write(f'{c}\t{n}\t{mn}\n')
