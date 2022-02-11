provider "azurerm" {
  features {}
}

data "azurerm_key_vault_secret" "launch_darkly_sdk_key" {
  name = "launchDarkly-sdk-key"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}
