# TODO: update function location and use java 11
variable "region" {}
variable "function_name" {}

provider "aws" {
  region = var.region
  shared_credentials_files = ["~/.aws/credentials"]
}

data "archive_file" "lambda_zip" {
  type        = "zip"
  source_dir  = "../lambda_function"
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
  timeout = 900

  filename = data.archive_file.lambda_zip.output_path
}

output "lambda_arn" {
  description = "The ARN of the Lambda function"
  value       = aws_lambda_function.lambda.arn
}

output "lambda_function_name" {
  description = "The name of the Lambda function"
  value       = aws_lambda_function.lambda.function_name
}