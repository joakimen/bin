#!/usr/bin/env bash
set -eo pipefail

group=$(aws logs describe-log-groups | jq ".logGroups[].logGroupName" -r | fzf)
aws logs tail --since ${1:-3d} "$group"
