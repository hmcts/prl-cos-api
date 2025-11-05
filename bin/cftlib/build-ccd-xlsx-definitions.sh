#!/usr/bin/env bash

orchestrator_dir="$PWD"

mkdir "$orchestrator_dir"/build/definitionsToBeImported

cd $(find ../ -name prl-ccd-definitions -maxdepth 1 -mindepth  1 -type d)
yarn generate-excel-local

mv definitions/private-law/xlsx/ccd-config-PRL-local.xlsx "$orchestrator_dir"/build/definitionsToBeImported/ccd-config-PRL-local.xlsx
