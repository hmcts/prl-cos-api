variable "common_tags" {
  type = map(any)
}

variable "product" {
  type    = string
  default = "prl"
}

variable "component" {
  type    = string
  default = "cos"
}

variable "env" {
  type = string
}
