"""Checks that every SRG id (m_123_ / f_123_) used by the coremod scripts and the reflection helpers exists in the production jars.
usage: python srgcheck.py <prod-test/<ver> folder>  (needs the installed server and client: the *-srg.jar files under libraries/)"""
import collections
import glob
import os
import re
import sys
import zipfile

prod = sys.argv[1]
jars = glob.glob(os.path.join(prod, 'libraries', 'net', 'minecraft', 'server', '*', 'server-*-srg.jar'))
jars += glob.glob(os.path.join(prod, 'client', 'libraries', 'net', 'minecraft', 'client', '*', 'client-*-srg.jar'))
ids = collections.defaultdict(set)
for pattern in ('src/main/resources/transformer/*.js', 'src/main/java/**/*.java'):
    for f in glob.glob(pattern, recursive=True):
        for m in re.findall(r'"([mf]_\d+_)"', open(f, encoding='utf-8', errors='replace').read()) + re.findall(r"'([mf]_\d+_)'", open(f, encoding='utf-8', errors='replace').read()):
            ids[m].add(os.path.basename(f))
found = {}
for j in jars:
    z = zipfile.ZipFile(j)
    for n in z.namelist():
        if not n.endswith('.class'):
            continue
        b = z.read(n)
        for i in ids:
            if i not in found and i.encode() in b:
                found[i] = n
for i in sorted(ids):
    print(i, ('OK  ' + found[i]) if i in found else 'MISSING', sorted(ids[i]))
