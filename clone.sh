#!/usr/bin/env bash
set -eo pipefail

# clones one of my github repos

if [[ $# -eq 2 ]]; then
  repo=$1/$2
else
  repo=$(gh repo list --limit 1000 | choose 0 | fzf)
fi
repoPath=$HOME/dev/github.com/$repo
[[ -d "$repoPath" ]] && {
  echo "$0: $repoPath: Directory exists, exiting"
  cd "$repoPath"
  return
}

gh repo clone "$repo" "$repoPath"
echo "$repoPath"
