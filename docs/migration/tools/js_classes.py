import re,glob,pickle
classes,_,_=pickle.load(open('maps.pkl','rb'))
miss=set()
def conv(m):
    t=m.group(0)
    slash='/' in t
    dotted=t.replace('/','.')
    if dotted in classes:
        n=classes[dotted]
        return n.replace('.','/') if slash else n
    miss.add(t); return t
pat=re.compile(r'(?:net[./]minecraft|com[./]mojang)[./][a-z0-9_./]+[./][A-Z]\w*(?:\$\w+)*')
for f in glob.glob('D:/Projects/Clauding/Random-Things/1.18.2/src/main/resources/transformer/*.js'):
    s=open(f,encoding='utf8',newline='').read(); n=pat.sub(conv,s)
    if n!=s: open(f,'w',encoding='utf8',newline='').write(n)
print('unmapped',sorted(miss))
