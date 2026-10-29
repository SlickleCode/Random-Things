#!/bin/bash
# usage: bash docs/testing/fixture-chain.sh   (from any version folder; about 40 minutes, uses the desktop: the game is opened 17 times)
# old-world loading: each branch builds a fixture world, the next branch opens it and verifies it
ROOT=$(dirname "$(git rev-parse --show-toplevel)")      # the folder that holds one worktree per version
ORDER="1.14.4 1.16.5 1.18.2 1.19.2 1.20.1 1.20.6 1.21.1 1.21.4 1.21.11"
prev=""
for v in $ORDER; do
  cd $ROOT/$v
  source env/activate.sh >/dev/null 2>&1
  if [ -n "$prev" ]; then
    rm -rf run/saves/RTFixture run/randomthings_fixture.txt
    cp -r $ROOT/$prev/run/saves/RTFixture run/saves/RTFixture
    RT_AUTOTEST=true RT_FIXTURE=verify timeout 900 ./gradlew runClient > run/fixture_verify.log 2>&1
    echo "== $prev -> $v"
    if grep -q "RT-DEBUG\] fixture: " run/logs/latest.log 2>/dev/null; then grep "RT-DEBUG\] fixture: " run/logs/latest.log | sed "s/.*fixture: //" | cut -c1-200; else echo "NO REPORT (crashed or never reached the world): $(grep -m1 -E "Exception|Caused by|What went wrong" -A1 run/fixture_verify.log | tr "
" " " | cut -c1-240)"; fi
  fi
  RT_AUTOTEST=true RT_FIXTURE=build timeout 900 ./gradlew runClient > run/fixture_build.log 2>&1
  if [ ! -f run/saves/RTFixture/rt_fixture_expected.txt ]; then echo "== $v: BUILD FAILED: $(grep -m1 -E 'Exception|Caused by|What went wrong' -A1 run/fixture_build.log | tr '\n' ' ' | cut -c1-240)"; fi
  prev=$v
done
echo CHAINDONE
