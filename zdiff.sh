#!/usr/bin/env bash
set -e
file=$(git diff --name-only | fzf --preview "git diff --color=always -- {}")
lazygit -f "$file"
