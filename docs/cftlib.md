# RSE CFT lib
This page provides instructions on how to develop and test `prl-cos-api` in a local development
environment using [cftlib](https://github.com/hmcts/rse-cft-lib). AAT is used to provide some of the dependent services.

## Limitations
There is currently no support for
- Work Allocation
- Hearings

## Prerequisites
1. Docker must be running.
2. You must be connected to the VPN.
3. An `.aat-env` file is needed to provide the environment variables required to point services at AAT.
   As these include Azure secrets, it is not stored in Git. If the file is not already present then it is downloaded automatically when the local development environment is started.

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

| Application         | URL                   |
|---------------------|-----------------------|
| Manage Cases        | http://localhost:3000 |
| Manage Organisation | http://localhost:3001 |

## Importing CCD definitions
The [prl-ccd-definitions](https://github.com/hmcts/prl-ccd-definitions) are imported automatically on cftlib startup.

In order for the definitions to be imported you must have checked out
[prl-ccd-definitions](https://github.com/hmcts/prl-ccd-definitions) in the same parent directory as `prl-cos-api`.
