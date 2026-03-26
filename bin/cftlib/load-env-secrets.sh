#!/bin/bash

# Script to fetch secret from Azure Key Vault and write to .aat-env if it does not exist

ENV_FILE=".aat-env"

if [ ! -f "$ENV_FILE" ]; then
  az keyvault secret show \
    --vault-name prl-aat \
    -o tsv \
    --query value \
    --name prl-cos-cft-lib-local-config \
    > "$ENV_FILE"
fi

