#!/usr/bin/env bash
# Apply one commit's diff to other version worktrees (working tree only, NO commit), then compile each.
#
#   bash docs/migration/tools/mirror.sh [--no-compile] <sha> <branch> [<branch>...]
#
# Run it from any version folder; targets are the sibling worktrees (<parent>/<branch>). A target where the patch does
# not apply is left untouched and reported "needs manual port". Nothing is committed or pushed; review and commit per branch.
# Hop one version at a time (1.19.2 -> 1.20.1 -> 1.20.6): a patch from far away rarely applies, and API changes
# (see the branch-mirroring skill) are never mechanical.
set -u

compile=1
if [ "${1:-}" = "--no-compile" ]; then compile=0; shift; fi
if [ $# -lt 2 ]; then echo "usage: mirror.sh [--no-compile] <sha> <branch>..."; exit 2; fi

sha=$1; shift
here=$(git rev-parse --show-toplevel) || exit 2
parent=$(dirname "$here")
git rev-parse --verify -q "$sha^{commit}" >/dev/null || { echo "unknown commit: $sha"; exit 2; }

patch=$(mktemp)
trap 'rm -f "$patch"' EXIT
git diff --binary "$sha^" "$sha" > "$patch"
touches_code=0
git diff --name-only "$sha^" "$sha" | grep -q '^src/\|^build.gradle$' && touches_code=1

printf 'commit %s: %s\n\n' "$(git rev-parse --short "$sha")" "$(git log -1 --format=%s "$sha")"
rows=()
for b in "$@"; do
	dir="$parent/$b"
	if [ ! -d "$dir" ]; then rows+=("$b|no such worktree|-"); continue; fi
	if ! git -C "$dir" apply --check --whitespace=nowarn < "$patch" 2>/dev/null; then
		rows+=("$b|needs manual port (patch does not apply)|-"); continue
	fi
	git -C "$dir" apply --whitespace=nowarn < "$patch"
	files=$(git -C "$dir" status --short | wc -l)
	if [ $compile -eq 0 ] || [ $touches_code -eq 0 ]; then
		rows+=("$b|applied|compile skipped"); continue
	fi
	log=$(mktemp)
	if (cd "$dir" && set +u; source env/activate.sh >/dev/null 2>&1; timeout 900 ./gradlew compileJava --offline -q) > "$log" 2>&1; then
		rows+=("$b|applied ($files files changed in tree)|compiles")
	else
		rows+=("$b|applied ($files files changed in tree)|COMPILE FAILED: $(grep -m3 'error:' "$log" | tr '\n' ' ' | cut -c1-240)")
	fi
	rm -f "$log"
done

printf '%-8s %-45s %s\n' BRANCH RESULT BUILD
for r in "${rows[@]}"; do IFS='|' read -r a b c <<< "$r"; printf '%-8s %-45s %s\n' "$a" "$b" "$c"; done
