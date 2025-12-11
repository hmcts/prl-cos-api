#!/bin/bash
# Reindex Elasticsearch in the cftlib local development environment.
#
# Usage: reindex-elasticsearch.sh
#
# This script assumes the following:
# - prl-ccd-definitions repository is located in the same parent directory as prl-cos-api
# - the following environment variables are set:
#   - PRL_COS_CLIENT_SECRET_AAT
#   - CCD_IMPORT_USERNAME_AAT
#   - CCD_IMPORT_PASSWORD_AAT

echo "Reindexing cases"
curl -XDELETE localhost:9200/prlapps_cases-000001
echo
./bin/cftlib/import-ccd-definitions.sh
if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to import CCD definitions"
  exit 1
fi

echo
echo "Updating case_data table"
docker exec -t cftlib-shared-database-pg-1 \
  psql -U postgres -d datastore -c "update case_data set marked_by_logstash = false where case_type_id = 'PRLAPPS';"
