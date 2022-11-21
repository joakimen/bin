#!/usr/bin/env bash

# open a sandboxed chrome-session with the specified aws profile credentials

profile="$1"
[[ -z "$profile" ]]; then
    echo "Profile is a required argument" >&2
    return 1
fi

# replace non word and not - with __
profile_dir_name=${profile//[^a-zA-Z0-9_-]/__}
browser_data=$(mktemp -d /tmp/awschrome_userdata.XXXXXXXX)
new_window_arg=''

# run aws-vault
# --prompt osascript only works on OSX
url=$(aws-vault login $profile --stdout --prompt osascript)
status=$?

if [[ ${status} -ne 0 ]]; then
    # bash will also capture stderr, so echo $url
    echo ${url}
    return ${status}
fi

mkdir -p ${user_data_dir}
browser_cache=$(mktemp -d /tmp/awschrome_cache.XXXXXXXX)
/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome \
    --no-first-run \
    --user-data-dir=${browser_data} \
    --disk-cache-dir=${browser_cache} \
    ${new_window_arg} \
    ${url} \
  >/dev/null 2>&1 &
