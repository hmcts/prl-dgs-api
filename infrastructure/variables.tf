variable "product" {}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}


variable "common_tags" {
  type = map(string)
}

variable "ccd_case_docs_am_api_health_endpoint" {
  default = "/health"
}
