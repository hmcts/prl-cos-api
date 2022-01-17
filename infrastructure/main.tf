provider "azurerm" {
  features {}
}

locals {
  vaultName = "${var.raw_product}-${var.env}"
}

data "azurerm_key_vault" "prl_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "test-secret" {
  name = "test-secret"
  key_vault_id = "${data.azurerm_key_vault.prl_key_vault.id}"
}
