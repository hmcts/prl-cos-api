provider "azurerm" {
  features {}
}

module "application_insights" {
  source = "git@github.com:hmcts/terraform-module-application-insights?ref=main"

  env     = var.env
  product = var.product
  name    = "${var.product}-${var.component}-appinsights"

  resource_group_name = azurerm_resource_group.rg.name
  location            = var.appinsights_location
  common_tags         = var.common_tags
}
