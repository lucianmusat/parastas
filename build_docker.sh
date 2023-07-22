#!/bin/bash

get_debian_codename() {
  . /etc/os-release
  echo $VERSION_CODENAME
}

case $(uname -m) in
  x86_64)
    DOCKERFILE="Dockerfile.x86"
    ;;
  arm*|aarch*|armv*)
    DOCKERFILE="Dockerfile.arm"
    ;;
  *)
    echo "Unsupported platform"
    exit 1
    ;;
esac

DEBIAN_CODENAME=$(get_debian_codename)

docker build -t parastas --build-arg DEBIAN_CODENAME=$DEBIAN_CODENAME -f $DOCKERFILE .
