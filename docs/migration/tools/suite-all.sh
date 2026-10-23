#!/usr/bin/env bash
# Run the dedicated-server test suite on several version worktrees, one after another, and print one table.
#
#   bash docs/migration/tools/suite-all.sh [--baseline] [branch...]     (default: every version worktree)
#
# Per branch: kills stray java, clears the old test world/reports, runs `RT_AUTOTEST=true RT_AUTOTEST_EXIT=true ./gradlew runServer`,
# reads run/randomthings_tests.txt (trust the report, not the Gradle exit code). Failing tests are listed under the table.
# Normal-world pass: a branch with docs/testing/normal-world-pass.txt (first line = a test name filter) gets a second run of just those
# tests in a normal-terrain server world (run/server.properties is patched to level-type=minecraft:normal, level-seed=424242 so the terrain is the same every run, and restored); it is reported as
# its own row "<branch>+normal". The default suite world is flat, so tests that need real terrain skip in the first pass.
# --baseline compares with docs/testing/suite-baseline.txt (lines "<row> <passed> <failed> <skipped>") and writes the file when it is missing.
# Never commits. Runs are sequential on purpose (shared CPU, one Gradle daemon each). Client/GUI/scene suites need a window: use suite-runner.
set -u

baseline=0
if [ "${1:-}" = "--baseline" ]; then baseline=1; shift; fi
here=$(git rev-parse --show-toplevel) || exit 2
parent=$(dirname "$here")
branches=("$@")
[ ${#branches[@]} -eq 0 ] && mapfile -t branches < <(ls "$parent" | grep -E '^[0-9]+\.[0-9]+(\.[0-9]+)?$' | sort -V)
base_file="$here/docs/testing/suite-baseline.txt"

rows=(); fails=()

# run_pass <dir> <row label> <test filter or empty> <normal-world pass: true/false>: runs the server suite, appends to rows/fails
run_pass() {
	local dir=$1 label=$2 filter=$3 normal=$4 log rep p f s note bp bf bs
	taskkill //F //IM java.exe >/dev/null 2>&1
	rm -rf "$dir/run/RTServerTest" "$dir/run/RTServerTestNormal" "$dir"/run/randomthings_*.txt
	log=$(mktemp)
	# --offline when the caches are warm; NeoGradle (1.20.6+) needs the network for its first download, so retry online without a report
	( cd "$dir" && set +u; source env/activate.sh >/dev/null 2>&1; RT_AUTOTEST=true RT_AUTOTEST_EXIT=true RT_AUTOTEST_FILTER="$filter" RT_AUTOTEST_NORMAL="$normal" timeout 1200 ./gradlew runServer --offline -q ) > "$log" 2>&1
	rep="$dir/run/randomthings_tests.txt"
	if [ ! -f "$rep" ]; then
		( cd "$dir" && set +u; source env/activate.sh >/dev/null 2>&1; RT_AUTOTEST=true RT_AUTOTEST_EXIT=true RT_AUTOTEST_FILTER="$filter" RT_AUTOTEST_NORMAL="$normal" timeout 1200 ./gradlew runServer -q ) > "$log" 2>&1
	fi
	if [ ! -f "$rep" ]; then
		rows+=("$label|-|-|-|NO REPORT: $(grep -m1 -iE "error:|exception|FAILED|not found" "$log" | cut -c1-120) (log: $log)"); return
	fi
	read -r p f s < <(grep -m1 'in-game tests:' "$rep" | sed -E 's/.*: ([0-9]+) passed, ([0-9]+) failed, ([0-9]+) skipped.*/\1 \2 \3/')
	note=""
	if [ $baseline -eq 1 ] && [ -f "$base_file" ]; then
		read -r _ bp bf bs < <(grep "^$label " "$base_file" || echo "$label ? ? ?")
		if [ "$bp" = "?" ]; then note="no baseline"; elif [ "$p $f $s" = "$bp $bf $bs" ]; then note="= baseline"; else note="BASELINE WAS $bp/$bf/$bs"; fi
	fi
	rows+=("$label|$p|$f|$s|$note")
	while IFS= read -r line; do fails+=("$label: ${line:0:200}"); done < <(grep '^FAIL ' "$rep")
	rm -f "$log"
}

for b in "${branches[@]}"; do
	dir="$parent/$b"
	[ -d "$dir" ] || { rows+=("$b|-|-|-|no such worktree"); continue; }
	run_pass "$dir" "$b" "" false

	pass_file="$dir/docs/testing/normal-world-pass.txt"
	props="$dir/run/server.properties"
	if [ -f "$pass_file" ] && [ -f "$props" ]; then
		cp "$props" "$props.suite-bak"
		{ grep -v -E '^(level-type|level-name|level-seed)=' "$props.suite-bak"; echo 'level-type=minecraft\:normal'; echo 'level-name=RTServerTestNormal'; echo 'level-seed=424242'; } > "$props"
		run_pass "$dir" "$b+normal" "$(head -n 1 "$pass_file" | tr -d '\r')" true
		mv -f "$props.suite-bak" "$props"
		rm -rf "$dir/run/RTServerTestNormal"
	fi
done

printf '%-14s %7s %6s %7s  %s\n' BRANCH PASSED FAILED SKIPPED NOTE
for r in "${rows[@]}"; do IFS='|' read -r a p f s n <<< "$r"; printf '%-14s %7s %6s %7s  %s\n' "$a" "$p" "$f" "$s" "$n"; done
if [ ${#fails[@]} -gt 0 ]; then printf '\nFAILURES\n'; printf '%s\n' "${fails[@]}"; fi

if [ $baseline -eq 1 ] && [ ! -f "$base_file" ]; then
	for r in "${rows[@]}"; do IFS='|' read -r a p f s n <<< "$r"; [ "$p" != "-" ] && echo "$a $p $f $s"; done > "$base_file"
	echo "wrote $base_file"
fi
