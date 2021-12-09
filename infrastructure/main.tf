provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
}
locals {

  previewVaultName    = "${var.reform_team}-aat"
  nonPreviewVaultName = "${var.reform_team}-${var.env}"
  vaultName           = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"
  vaultUri            = data.azurerm_key_vault.prl_key_vault.vault_uri
}

data "azurerm_key_vault" "prl_key_vault" {
  name                = local.vaultName
  resource_group_name = local.vaultName
}

data "azurerm_key_vault_secret" "system-update-user-username" {
  name      = "system-update-user-username"
  key_vault_id = data.azurerm_key_vault.prl_key_vault.id
}

data "azurerm_key_vault_secret" "system-update-user-password" {
  name      = "system-update-user-password"
  key_vault_id = data.azurerm_key_vault.prl_key_vault.id
}

data "azurerm_key_vault_secret" "idam-secret" {
  name      = "idam-secret"
  key_vault_id = data.azurerm_key_vault.prl_key_vault.id
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "s2s_key_from_vault" {
  name         = "microservicekey-prl-cos-api"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

resource "azurerm_key_vault_secret" "s2s" {
  name         = "s2s-secret"
  value        = data.azurerm_key_vault_secret.s2s_key_from_vault.value
  key_vault_id = data.azurerm_key_vault.prl_key_vault.id
}
