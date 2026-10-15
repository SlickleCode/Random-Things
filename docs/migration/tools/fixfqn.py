import re,glob,pickle
classes,_,_=pickle.load(open('maps.pkl','rb'))
rep={}
for old,moj in classes.items():
    parts=old.replace('$','.').split('.')
    newsimple=moj.replace('$','.').split('.')[-1]
    key='.'.join(parts[:-1]+[newsimple])
    rep[key]=moj.replace('$','.')
    rep['.'.join(parts)]=moj.replace('$','.')
forge={'net.minecraftforge.fml.network.':'net.minecraftforge.network.','net.minecraftforge.fml.common.ObfuscationReflectionHelper':'net.minecraftforge.fml.util.ObfuscationReflectionHelper'}
pat=re.compile(r'\bnet\.minecraft\.[a-z0-9_.]*[a-z0-9_]\.[A-Z]\w*(?:\.[A-Z]\w*)*')
n=0
for f in glob.glob('D:/Projects/Clauding/Random-Things/1.18.2/src/**/*.java',recursive=True):
    s=open(f,encoding='utf8').read(); o=s
    def r(m):
        t=m.group(0)
        # try longest prefix
        segs=t.split('.')
        for i in range(len(segs),0,-1):
            k='.'.join(segs[:i])
            if k in rep and rep[k]!=k:
                return rep[k]+('.'+'.'.join(segs[i:]) if i<len(segs) else '')
        return t
    s=pat.sub(r,s)
    for a,b in forge.items(): s=s.replace(a,b)
    if s!=o: open(f,'w',encoding='utf8',newline='').write(s); n+=1
print(n)
