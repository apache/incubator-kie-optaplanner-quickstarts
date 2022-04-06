#!/bin/bash

function display_help() {
  readonly script_name="./$(basename "$0")"

  echo "This script uploads the OptaPlanner distribution to the filemgmt-prod.jboss.org."
  echo "Make sure the OptaPlanner Quickstarts project has been build with the full profile enabled before calling this script."
  echo
  echo "Usage:"
  echo "  $script_name PROJECT_VERSION SSH_KEY"
  echo "  $script_name --help"
}

function create_latest_symlinks() {
  local _working_directory=$1
  local _version=$2

  pushd "$_working_directory"
  cd "$_working_directory"
  ln -s "$_version" latest
  if [[ "$_version" == *Final* ]]; then
    ln -s "$_version" latestFinal
  fi
  popd
}

if [[ $1 == "--help" ]]; then
  display_help
  exit 0
fi

if [[ $# -ne 2 ]]; then
  echo "Illegal number of arguments."
  display_help
  exit 1
fi

readonly remote_optaplanner_downloads=optaplanner@filemgmt-prod-sync.jboss.org:/downloads_htdocs/optaplanner

readonly version=$1
readonly optaplanner_ssh_key=$2

this_script_directory="${BASH_SOURCE%/*}"
if [[ ! -d "$this_script_directory" ]]; then
  this_script_directory="$PWD"
fi

readonly quickstarts_project_root=$this_script_directory/../..
readonly optaplanner_distribution=$quickstarts_project_root/build/optaplanner-distribution
readonly optaplanner_distribution_build_dir="$optaplanner_distribution/target/optaplanner-distribution-$version"

if [[ ! -d "$optaplanner_distribution_build_dir" ]]; then
  echo "The $optaplanner_distribution_build_dir directory does not exist. Please run the build with the full profile enabled."
  exit 1
fi

# Create the directory structure .../release/${version}
readonly temp_release_directory=/tmp/optaplanner-release-$version
readonly local_optaplanner_downloads=$temp_release_directory/downloads/release

if [ -d "$temp_release_directory" ]; then
  rm -Rf "$temp_release_directory";
fi

mkdir -p "$local_optaplanner_downloads/$version"

# Upload the OptaPlanner distribution.zip.
cp "$optaplanner_distribution/target/optaplanner-distribution-$version.zip" "$local_optaplanner_downloads/$version"

readonly remote_shell="ssh -p 2222 -i $optaplanner_ssh_key"
create_latest_symlinks "$local_optaplanner_downloads" "$version"
rsync -a -r -e "$remote_shell" --protocol=28 "$local_optaplanner_downloads/.." "$remote_optaplanner_downloads"
