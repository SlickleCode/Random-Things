import re,sys,collections
def parse(fn):
    L=open(fn,encoding='utf8',errors='replace').read().split('\n'); out=[]
    i=0
    while i<len(L):
        m=re.match(r'(.*\.java):(\d+): error: (.*)',L[i])
        if m:
            j=i+1; extra=[]
            while j<len(L) and not re.match(r'.*\.java:\d+: (error|warning)',L[j]) and not L[j].startswith('Note:') and not L[j].startswith('FAIL') and not re.match(r'\d+ error',L[j]): extra.append(L[j]); j+=1
            caret=None
            for e in extra:
                if e.strip()=='^': caret=e.index('^')
            out.append((m.group(1),int(m.group(2)),m.group(3),extra,caret)); i=j
        else: i+=1
    return out
def apply(fn_errs, msgpat, old, new):
    errs=[e for e in fn_errs if re.search(msgpat,e[2])]
    byfile=collections.defaultdict(list)
    for e in errs: byfile[e[0]].append(e)
    n=0
    for f,es in byfile.items():
        raw=open(f,encoding='utf8',newline='').read()
        lines=raw.split('\n')
        for (_,ln,msg,extra,caret) in sorted(es,key=lambda x:(-x[1],-(x[4] or 0))):
            s=lines[ln-1]
            # expand tabs same as javac? javac prints source line verbatim; caret col aligned to that
            c=caret if caret is not None else len(s)
            idx=s.rfind(old,0,c+len(old))
            if idx>=0:
                lines[ln-1]=s[:idx]+new+s[idx+len(old):]; n+=1
        open(f,'w',encoding='utf8',newline='').write('\n'.join(lines))
    return n
if __name__=='__main__':
    errs=parse(sys.argv[1]); print(apply(errs,sys.argv[2],sys.argv[3],sys.argv[4]))
