#!/usr/bin/env bash

# open a sandboxed chrome-session with the specified aws profile credentials

profile="$1"
if [[ -z "$profile" ]]; then
    echo "usage: $0 <aws-profile>" >&2
    exit 1
fi

# run aws-vault
url=$(aws-vault login "$profile" --stdout --prompt osascript)
status=$?

if [[ $status -ne 0 ]]; then
    # bash will also capture stderr, so echo $url
    echo "$url"
    exit $status
fi

/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome \
    --no-first-run \
    --user-data-dir="$(mktemp -d /tmp/awschrome_userdata.XXXXXXXX)" \
    --disk-cache-dir="$(mktemp -d /tmp/awschrome_cache.XXXXXXXX)" \
    "$url" \
    >/dev/null 2>&1 &
