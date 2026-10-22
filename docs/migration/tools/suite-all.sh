#!/usr/bin/env bash
# Run the dedicated-server test suite on several version worktrees, one after another, and print one table.
#
#   bash docs/migration/tools/suite-all.sh [--baseline] [branch...]     (default: all six branches)
#
# Per branch: kills stray java, clears the old test world/reports, runs `RT_AUTOTEST=true RT_AUTOTEST_EXIT=true ./gradlew runServer`,
# reads run/randomthings_tests.txt (trust the report, not the Gradle exit code). Failing tests are listed under the table.
# --baseline compares with docs/testing/suite-baseline.txt (lines "<branch> <passed> <failed> <skipped>") and writes the file when it is missing.
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
for b in "${branches[@]}"; do
	dir="$parent/$b"
	[ -d "$dir" ] || { rows+=("$b|-|-|-|no such worktree"); continue; }
	taskkill //F //IM java.exe >/dev/null 2>&1
	rm -rf "$dir/run/RTServerTest" "$dir"/run/randomthings_*.txt
	log=$(mktemp)
	# --offline when the caches are warm; NeoGradle (1.20.6) needs the network for its first download, so retry online without a report
	( cd "$dir" && set +u; source env/activate.sh >/dev/null 2>&1; RT_AUTOTEST=true RT_AUTOTEST_EXIT=true timeout 1200 ./gradlew runServer --offline -q ) > "$log" 2>&1
	rep="$dir/run/randomthings_tests.txt"
	if [ ! -f "$rep" ]; then
		( cd "$dir" && set +u; source env/activate.sh >/dev/null 2>&1; RT_AUTOTEST=true RT_AUTOTEST_EXIT=true timeout 1200 ./gradlew runServer -q ) > "$log" 2>&1
	fi
	if [ ! -f "$rep" ]; then
		rows+=("$b|-|-|-|NO REPORT: $(grep -m1 -iE "error:|exception|FAILED|not found" "$log" | cut -c1-120) (log: $log)"); continue
	fi
	read -r p f s < <(grep -m1 'in-game tests:' "$rep" | sed -E 's/.*: ([0-9]+) passed, ([0-9]+) failed, ([0-9]+) skipped.*/\1 \2 \3/')
	note=""
	if [ $baseline -eq 1 ] && [ -f "$base_file" ]; then
		read -r _ bp bf bs < <(grep "^$b " "$base_file" || echo "$b ? ? ?")
		if [ "$bp" = "?" ]; then note="no baseline"; elif [ "$p $f $s" = "$bp $bf $bs" ]; then note="= baseline"; else note="BASELINE WAS $bp/$bf/$bs"; fi
	fi
	rows+=("$b|$p|$f|$s|$note")
	while IFS= read -r line; do fails+=("$b: ${line:0:200}"); done < <(grep '^FAIL ' "$rep")
	rm -f "$log"
done

printf '%-8s %7s %6s %7s  %s\n' BRANCH PASSED FAILED SKIPPED NOTE
for r in "${rows[@]}"; do IFS='|' read -r a p f s n <<< "$r"; printf '%-8s %7s %6s %7s  %s\n' "$a" "$p" "$f" "$s" "$n"; done
if [ ${#fails[@]} -gt 0 ]; then printf '\nFAILURES\n'; printf '%s\n' "${fails[@]}"; fi

if [ $baseline -eq 1 ] && [ ! -f "$base_file" ]; then
	for r in "${rows[@]}"; do IFS='|' read -r a p f s n <<< "$r"; [ "$p" != "-" ] && echo "$a $p $f $s"; done > "$base_file"
	echo "wrote $base_file"
fi
