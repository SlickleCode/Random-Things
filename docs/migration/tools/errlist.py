import re,sys
t=open(sys.argv[1],encoding='utf-8',errors='replace').read().split('\n')
for i,l in enumerate(t):
    m=re.match(r'^(.+\.java):(\d+): error: (.*)$',l)
    if m:
        f=re.split(r'randomthings[\/]',m.group(1))[-1]
        src=t[i+1].strip()[:120]
        sym=next((t[k].strip().replace('symbol:','') for k in range(i+1,min(i+6,len(t))) if 'symbol:' in t[k]),'')
        print('%s:%s %s |%s| %s'%(f,m.group(2),m.group(3)[:80],sym,src))
