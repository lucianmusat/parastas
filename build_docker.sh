#!/bin/bash

get_debian_codename() {
  # Check if running on Linux with /etc/os-release
  if [ -f /etc/os-release ]; then
    . /etc/os-release
    echo $VERSION_CODENAME
  else
    # Default to bullseye for non-Linux systems (like macOS)
    echo "bullseye"
  fi
}

# Detect architecture and set appropriate Dockerfile and platform
ARCH=$(uname -m)
case $ARCH in
  x86_64|amd64)
    DOCKERFILE="Dockerfile.x86"
    PLATFORM="linux/amd64"
    ARCH_NAME="x86_64/AMD64"
    ;;
  arm64|aarch64)
    # ARM64 - M2 Mac, newer Raspberry Pi, AWS Graviton
    DOCKERFILE="Dockerfile.arm64"
    PLATFORM="linux/arm64"
    ARCH_NAME="ARM64"
    ;;
  armv7l|armv7*)
    # ARM32v7 - Older Raspberry Pi (Pi 2, Pi 3)
    DOCKERFILE="Dockerfile.arm"
    PLATFORM="linux/arm/v7"
    ARCH_NAME="ARM32v7"
    ;;
  armv6l)
    # ARM32v6 - Very old Raspberry Pi (Pi Zero, Pi 1)
    DOCKERFILE="Dockerfile.arm"
    PLATFORM="linux/arm/v6"
    ARCH_NAME="ARM32v6"
    ;;
  *)
    echo "Unsupported architecture: $ARCH"
    exit 1
    ;;
esac

DEBIAN_CODENAME=$(get_debian_codename)

echo "========================================"
echo "Building Docker image for $ARCH_NAME"
echo "Architecture: $ARCH"
echo "Platform: $PLATFORM"
echo "Dockerfile: $DOCKERFILE"
echo "Debian codename: $DEBIAN_CODENAME"
echo "========================================"

# Use buildx for cross-platform builds and specify platform
docker buildx build --platform $PLATFORM -t parastas --build-arg DEBIAN_CODENAME=$DEBIAN_CODENAME -f $DOCKERFILE --load .
