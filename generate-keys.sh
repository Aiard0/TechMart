#!/bin/bash
set -euo pipefail

KEY_DIR="${1:-src/main/resources}"

if [ ! -d "$KEY_DIR" ]; then
  echo "Directory $KEY_DIR does not exist"
  exit 1
fi

echo "Generating RSA 2048 key pair in $KEY_DIR..."

openssl genrsa -out "$KEY_DIR/privateKey.pem" 2048
openssl rsa -pubout -in "$KEY_DIR/privateKey.pem" -out "$KEY_DIR/publicKey.pem"

chmod 600 "$KEY_DIR/privateKey.pem"
chmod 644 "$KEY_DIR/publicKey.pem"

echo "Done:"
echo "  Private key: $KEY_DIR/privateKey.pem"
echo "  Public key:  $KEY_DIR/publicKey.pem"
