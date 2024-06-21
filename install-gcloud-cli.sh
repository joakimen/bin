#!/usr/bin/env bash
set -euo pipefail

CLI_VERSION="481.0.0"
arch=$(uname -sm)

function err_exit() {
  echo "Error: $1" >&2
  exit 1
}

filename_ident=""
case "$arch" in
Darwin\ arm64) filename_ident="darwin-arm.tar.gz" ;;
# add linux variants if needed
*) err_exit "unsupported architecture: $arch" ;;
esac

filename="google-cloud-sdk-${CLI_VERSION}-${filename_ident}"
url="https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/${filename}"

echo
echo "cli_version: $CLI_VERSION"
echo "arch: $arch"
echo "pwd: $(pwd)"
echo "url: $url"
echo

echo "continue?"
read -p "[y/N] " -r
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "exiting."
  exit 1
fi

echo

# check if google-cloud-sdk already exists
if [ -d "google-cloud-sdk" ]; then

  # ask user if they want to overwrite
  read -p "google-cloud-sdk directory already exists. do you want to overwrite it? [y/N] " -r
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "exiting."
    exit 1
  fi
  echo "removing old google-cloud-sdk directory..."
  rm -rvf "google-cloud-sdk"
fi

echo "downloading..."
curl -#SLO "$url"

echo
echo "extracting..."
tar -xzf "$filename"

echo
echo "cleaning up..."
rm "$filename"

echo "done."
echo
echo "please ensure that the 'google-cloud-sdk/bin' directory is in your PATH:"
echo "$(pwd)/google-cloud-sdk/bin"
