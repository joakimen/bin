#!/usr/bin/env bash

clear
echo "shell: $SHELL"
for i in $(seq 1 10); do /usr/bin/time $SHELL -i -c exit; done