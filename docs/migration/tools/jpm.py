import sys,subprocess,re,os
C=os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.18.2-40.3.12_mapped_official_1.18.2/forge-1.18.2-40.3.12_mapped_official_1.18.2.jar')
J=os.path.expanduser('~/.jdks/temurin-17.0.14/bin/javap')
cls,meth=sys.argv[1],sys.argv[2]
flt=sys.argv[3] if len(sys.argv)>3 else None
out=subprocess.run([J,'-c','-p','-cp',C,cls],capture_output=True,text=True).stdout.split('\n')
p=False
for l in out:
    if re.match(r'^  [^ ].*\(',l) or re.match(r'^  static \{\}',l):
        p=bool(re.search(r'\b'+meth+r'\(',l))
    if p and (not flt or re.search(flt,l) or re.match(r'^  [^ ]',l)): print(l)
