#!/usr/bin/env bash
set -euo pipefail

CLI_VERSION="481.0.0"
SDK_DIR="google-cloud-sdk"
arch=$(uname -sm)
base_dir=$(pwd)

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

tarball="google-cloud-sdk-${CLI_VERSION}-${filename_ident}"
url="https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/${tarball}"

echo
echo "cli_version: $CLI_VERSION"
echo "arch: $arch"
echo "pwd: $(pwd)"
echo "url: $url"
echo

read -p "continue? [y/N] " -r
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "exiting."
  exit 1
fi

echo
echo "downloading cli version ${CLI_VERSION}..."
curl -#SLO "$url"

# check if google-cloud-sdk already exists
if [ -d "google-cloud-sdk" ]; then

  echo

  # ask user if they want to overwrite
  read -p "google-cloud-sdk directory already exists. do you want to overwrite it? [y/N] " -r
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "exiting."
    exit 1
  fi
  echo "removing old google-cloud-sdk directory..."
  rm -rf "google-cloud-sdk"
fi

echo
echo "extracting..."
tar -xzf "$tarball"

echo
install_script="install.sh"
install_flags="--quiet --usage-reporting=false --command-completion=true --path-update=true"
echo "running google-cloud-sdk/install.sh..."

echo "installing gcloud with the following flags: $install_flags"
read -p "continue? [y/N] " -r
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "exiting."
  exit 1
fi

cd "$SDK_DIR"
# shellcheck disable=SC2086
bash "$install_script" $install_flags

echo "gcloud installation script completed."
echo "cleaning up..."
rm "${base_dir}/$tarball"

echo "done."

echo
echo "please ensure that the 'google-cloud-sdk/bin' directory is in your PATH:"
echo "$(pwd)/bin"
