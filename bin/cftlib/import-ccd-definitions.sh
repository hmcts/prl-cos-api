#!/bin/bash
# Imports the local versions of CCD definition files into an environment running CFTLib authMode = AAT.
# The local versions are automatically generated prior to import.
#
# Usage: import-ccd-definitions.sh
#
# This script assumes the following:
# - prl-ccd-definitions repository is located in the same parent directory as prl-cos-api
# - the following environment variables are set:
#   - PRL_COS_CLIENT_SECRET_AAT
#   - CCD_IMPORT_USERNAME_AAT
#   - CCD_IMPORT_PASSWORD_AAT

if [[ -z $PRL_COS_CLIENT_SECRET_AAT ]]; then
  echo 1>&2 "Error: environment variable PRL_COS_CLIENT_SECRET_AAT not set"
  exit 1
fi

if [[ -z $CCD_IMPORT_USERNAME_AAT ]]; then
  echo 1>&2 "Error: environment variable CCD_IMPORT_USERNAME_AAT not set"
  exit 1
fi

if [[ -z $CCD_IMPORT_PASSWORD_AAT ]]; then
  echo 1>&2 "Error: environment variable CCD_IMPORT_PASSWORD_AAT not set"
  exit 1
fi

idamApiHost=https://idam-api.aat.platform.hmcts.net
s2sHost=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
ccdDefinitionStoreHost=http://localhost:4451

cftlibFolder=$(dirname "$0")

echo "Requesting IDAM access token"
accessToken="$("$cftlibFolder"/idam-access-token.sh $idamApiHost "$PRL_COS_CLIENT_SECRET_AAT" "$CCD_IMPORT_USERNAME_AAT" "$CCD_IMPORT_PASSWORD_AAT")"

if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to get IDAM access token"
  exit 1
fi

echo "Requesting service token"
serviceToken="$("$cftlibFolder"/s2s-token.sh $s2sHost)"
if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to get service token"
  exit 1
fi

if [[ ${PWD##*/} = "bin" ]]; then
  cd ..
fi

if [[ ${PWD##*/} = "cftlib" ]]; then
  cd ../..
fi

cd "$(find ../ -name prl-ccd-definitions -maxdepth 1 -mindepth 1 -type d)" || exit 1

yarn generate-excel-local
if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to generate Excel file"
  exit 1
fi

excelFile="./definitions/private-law/xlsx/ccd-config-PRL-local.xlsx"
echo "Importing $excelFile"

response_code=$(curl --location "$ccdDefinitionStoreHost/import" \
  --header "Authorization: Bearer $accessToken" \
  --header "ServiceAuthorization: $serviceToken" \
  --header 'Content-Type: multipart/form-data' \
  --form "file=@$excelFile" \
  --silent --output /dev/null --write-out "%{http_code}")

if [[ $response_code -ne 201 ]]; then
  echo 1>&2 "Error: Failed to import file (HTTP $response_code)"
  exit 1
fi

echo "Import completed successfully"
exit 0
