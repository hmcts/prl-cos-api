# RSE CFT lib
This page provides instructions on how to develop and test `prl-cos-api` in a local development
environment using [cftlib](https://github.com/hmcts/rse-cft-lib). AAT is used to provide some of the dependent services.

## Limitations
There is currently no support for
- Work Allocation
- Hearings

## Prerequisites
1. The [prl-ccd-definitions](https://github.com/hmcts/prl-ccd-definitions) repository must be cloned in the same parent directory as `prl-cos-api`.
2. You must be connected to the VPN.
3. Docker must be running.
4. [jq](https://jqlang.org/) must be installed.
5. `.aat-env` file is needed to provide the environment variables required to point services at AAT.
   As these include Azure secrets, it is not stored in Git.
   If the file is not already present then it is downloaded automatically when the local development environment is started.

## Setup
The first time cftlib is run it needs to download Docker images from the Azure Registry. You must therefore login
to Azure as follows:
```bash
az acr login --name hmctspublic --subscription DCD-CNP-Prod
az acr login --name hmctsprivate --subscription DCD-CNP-Prod
```

## Running
```bash
./gradlew bootWithCCD
```

This will start `prl-cos-api` along with CCD common components and Docker containers for
ExUI, PostgreSQL, Elasticsearch and Logstash.

| Application         | URL                                                |
|---------------------|----------------------------------------------------|
| Manage Cases        | http://localhost:3000                              |
| Manage Organisation | http://localhost:3001                              |
| Elasticsearch       | http://localhost:9200/prlapps_cases-000001/_search |
| PostgreSQL          | localhost:6432                                     |

## Importing CCD definitions
The [prl-ccd-definitions](https://github.com/hmcts/prl-ccd-definitions) are imported automatically on cftlib startup.

If you need to re-import the definitions without restarting cftlib, you can do so by running:
```bash
./bin/cftlib/import-ccd-definitions.sh
```

You will need the following environment variables set:
- PRL_COS_CLIENT_SECRET_AAT
- CCD_IMPORT_USERNAME_AAT
- CCD_IMPORT_PASSWORD_AAT
