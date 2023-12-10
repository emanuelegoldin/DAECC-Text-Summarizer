variable "region" {}
variable "function_name" {}

provider "aws" {
  region = var.region
  shared_credentials_files = ["~/.aws/credentials"]
}

data "archive_file" "lambda_zip" {
  type        = "zip"
  source_dir  = "../AWS"            # replace with your zippped lambda function
  output_path = "lambda.zip"
}

data "aws_iam_role" "existing" {
  name = "LabRole"
}

resource "aws_lambda_function" "lambda" {
  function_name = var.function_name
  role          = data.aws_iam_role.existing.arn
  handler       = "lambda.lambda_handler"
  runtime       = "python3.9"

  filename = data.archive_file.lambda_zip.output_path
}