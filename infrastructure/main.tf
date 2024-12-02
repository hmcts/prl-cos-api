provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
  tags = var.common_tags
}


module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                    = "prl-aat"
  product                 = var.product
  env                     = var.env
  tenant_id               = var.tenant_id
  object_id               = var.jenkins_AAD_objectId
  resource_group_name     = azurerm_resource_group.rg.name
  product_group_name      = "DTS Family Private Law"
  product_group_object_id = var.jenkins_AAD_objectId
  common_tags             = var.common_tags
  create_managed_identity = true
}

locals {

  previewVaultName    = "${var.reform_team}-aat"
  nonPreviewVaultName = "${var.reform_team}-${var.env}"
  vaultName           = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"
  vaultUri            = data.azurerm_key_vault.prl_key_vault.vault_uri
}


data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
  name = "microservicekey-prl-cos-api"
}

resource "azurerm_key_vault_secret" "prl_s2s_secret" {
  name         = "microservicekey-prl-cos-api"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = module.key-vault.key_vault_id

  depends_on = [
    module.key-vault
  ]
}

data "azurerm_key_vault" "prl_key_vault" {
  name                = "prl-${var.env}"
  resource_group_name = "prl-${var.env}"
}

data "azurerm_key_vault_secret" "system-update-user-username" {
  name      = "system-update-user-username"
  key_vault_id = data.azurerm_key_vault.prl_key_vault.id
}

data "azurerm_key_vault_secret" "system-update-user-password" {
  name      = "system-update-user-password"
  key_vault_id = data.azurerm_key_vault.prl_key_vault.id
}

data "azurerm_key_vault_secret" "prl-cos-idam-client-secret" {
  name      = "prl-cos-idam-client-secret"
  key_vault_id = data.azurerm_key_vault.prl_key_vault.id
}

data "azurerm_key_vault_secret" "prl_cos_idam_client_secret" {
  key_vault_id = "${data.azurerm_key_vault.prl_key_vault.id}"
  name = "prl-cos-idam-client-secret"
}

resource "azurerm_key_vault_secret" "prl_idam_secret" {
  name         = "prl-cos-idam-client-secret"
  value        = data.azurerm_key_vault_secret.prl_cos_idam_client_secret.value
  key_vault_id = module.key-vault.key_vault_id

  depends_on = [
    module.key-vault
  ]
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
