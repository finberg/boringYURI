#!/bin/bash

#
# Copyright 2022 Anton Novikau
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

VERSION_PATTERN="[0-9]+\.[0-9]+\.[0-9]+"
VERSION_VAR_NAME="VERSION_NAME"

update_file() {
  if [ "$(uname)" == "Darwin" ]; then
    sed -i '' -r "$1" "$2"
  else
    sed -i -r "$1" "$2"
  fi
}

update_version() {
  local version=$1

  echo "Updating version to $version"

  update_file "s/^${VERSION_VAR_NAME}[[:blank:]]*=[[:blank:]]*($VERSION_PATTERN)[[:blank:]]*$/$VERSION_VAR_NAME = $version/" gradle.properties

  local readme_regex="s/^([[:blank:]]*[a-zA-Z]+([[:blank:]]+|\()\"com\.github\.anton-novikau:boringyuri-[a-z-]+:)$VERSION_PATTERN\"(\))?[[:blank:]]*$/\1$version\"\3/"
  update_file "$readme_regex" README.md
  update_file "$readme_regex" dagger/README.md
  update_file "$readme_regex" dagger-ksp/README.md
}

update_version "$1"