variable "region" {}
variable "model_name" {}
variable "image" {}
variable "model_data_url" {}

provider "aws" {
  region = var.region
  shared_credentials_files = ["~/.aws/credentials"]
}

data "aws_iam_role" "labrole" {
  name = "LabRole"
}

resource "aws_sagemaker_model" "mymodel" {
  name = var.model_name
  execution_role_arn = data.aws_iam_role.labrole.arn
  primary_container {
    image = var.image
    model_data_url = var.model_data_url
  }
}

resource "aws_sagemaker_endpoint_configuration" "myconfig" {
  name = "summarizer-endpoint-config"

  production_variants {
    variant_name           = "variant-1"
    model_name             = aws_sagemaker_model.mymodel.name
    initial_instance_count = 1
    instance_type          = "ml.p2.xlarge"
  }
}

resource "aws_sagemaker_endpoint" "myendpoint" {
  name                 = "summarizer-endpoint"
  endpoint_config_name = aws_sagemaker_endpoint_configuration.myconfig.name
}