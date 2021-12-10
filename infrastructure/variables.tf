variable "product" {}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}

variable "deployment_namespace" {}

variable "common_tags" {
  type = "map"
}

variable "ccd_case_docs_am_api_health_endpoint" {
  default = "/health"
}

variable "reform_service_name" {
  default = "prl"
}

variable "reform_team" {
  default = "prl"
}

variable "capacity" {
  default = "1"
}

variable "instance_size" {
  default = "I2"
}


variable "raw_product" {
  default = "prl"
}

variable "tenant_id" {}



variable "idam_s2s_url_prefix" {
  default = "rpe-service-auth-provider"
}


variable "auth_provider_service_client_tokentimetoliveinseconds" {
  default = "900"
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}
