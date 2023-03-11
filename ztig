#!/usr/bin/env bash
set -e
file=$(git ls-files | fzf --preview "git log --oneline -n 20 --color=always {}")
lazygit -f "$file"
