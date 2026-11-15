#!/bin/bash
# Real-village Peace Candle survey (debug/test/VillageSurvey.java): usage  bash docs/testing/survey-scripts/village_survey.sh [level-type]
# level-type: "default" (1.14.4 - 1.19.2, the default) or "minecraft\:normal" (1.20.1 and later).
# Builds, runs a dedicated server on a fresh normal world (seed 424242, watchdog off), probes a grid around spawn for the nearest
# villages and writes run/randomthings_villages.txt (and run/villages_<version>.txt): per village type the churches, the Peace
# Candle churches and whether the candle block really stands in the nearest one. Restores run/server.properties, deletes the world.
# RT_VILLAGE_SURVEY_RADIUS (blocks, default 4000) widens the grid; RT_VILLAGE_SURVEY_VANILLA=true switches the candle swap off for a comparison.
cd "$(dirname "$0")/../../.." || exit 1
v=$(basename "$(pwd)")
lt=${1:-default}
source env/activate.sh >/dev/null 2>&1
./gradlew build -x test -q >/tmp/build_s_$v.log 2>&1 || { echo "== $v BUILD FAILED"; grep -E "error:" /tmp/build_s_$v.log | head -6; exit 1; }
props=run/server.properties
cp $props $props.survey-bak 2>/dev/null
{ grep -v -E '^(level-type|level-name|level-seed|max-tick-time)=' $props.survey-bak 2>/dev/null; echo "level-type=$lt"; echo 'level-name=RTSurveyWorld'; echo 'level-seed=424242'; echo 'max-tick-time=-1'; } > $props
rm -rf run/RTSurveyWorld run/randomthings_villages.txt
opts="-Drandomthings.survey=true -Drandomthings.survey.radius=${RT_VILLAGE_SURVEY_RADIUS:-4000}"
[ "$RT_VILLAGE_SURVEY_VANILLA" = "true" ] && opts="$opts -Drandomthings.survey.vanilla=true"
JAVA_TOOL_OPTIONS="$opts" timeout 2400 ./gradlew runServer -q >/tmp/survey_$v.log 2>&1
if [ -f $props.survey-bak ]; then cp $props.survey-bak $props; rm -f $props.survey-bak; fi
rm -rf run/RTSurveyWorld
if [ -f run/randomthings_villages.txt ]; then cp run/randomthings_villages.txt run/villages_$v.txt; echo "== $v"; head -22 run/randomthings_villages.txt; else echo "== $v NO REPORT"; fi
