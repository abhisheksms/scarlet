#!/bin/bash
# Squash-merge a pull request only when its CI check has finished and passed.
#
# Usage: tools/merge_when_green.sh <pr-number>
#
# `gh pr checks --watch` can return early when a run is cancelled and replaced by a newer
# one (a second push to the same branch), so its exit code alone is not proof. This asks
# for the state of the named check on the PR's current head commit, and merges only on
# SUCCESS.
set -euo pipefail
PR="$1"
CHECK="Unit tests & debug build"

for _ in $(seq 1 90); do
  STATE=$(gh pr checks "$PR" --json name,state --jq ".[] | select(.name == \"$CHECK\") | .state" 2>/dev/null | head -1 || true)
  case "$STATE" in
    SUCCESS)
      gh pr merge "$PR" --squash --delete-branch
      echo "MERGED pr=$PR"
      exit 0
      ;;
    FAILURE|CANCELLED|TIMED_OUT|ERROR|ACTION_REQUIRED)
      echo "NOT MERGED pr=$PR check=$STATE"
      exit 1
      ;;
  esac
  sleep 20
done
echo "NOT MERGED pr=$PR: check did not finish in 30 minutes (last state: ${STATE:-none})"
exit 1
