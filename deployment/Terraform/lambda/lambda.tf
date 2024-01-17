# TODO: update function location and use java 11
variable "id" {}
variable "region" {}
variable "function_name" {}

provider "aws" {
  region = var.region
  shared_credentials_files = ["~/.aws/credentials"]
}

data "aws_iam_role" "existing" {
  name = "LabRole"
}

resource "aws_s3_bucket" "deployment_bucket" {
  bucket        = "deployment-packages-${var.region}-${var.id}"
  force_destroy = true
}

resource "aws_s3_object" "summarise_deployment_package" {
  bucket = aws_s3_bucket.deployment_bucket.id
  key    = "summarize.jar"
  acl    = "private"  # or can be "public-read"
  source = "${path.root}/../../DAECCProject/summarise/target/deployable/summarise-1.0-SNAPSHOT.jar"
  etag   = filemd5("${path.root}/../../DAECCProject/summarise/target/deployable/summarise-1.0-SNAPSHOT.jar")
}

resource "aws_lambda_function" "summarise_function" {
  function_name = var.function_name
  s3_bucket = aws_s3_object.summarise_deployment_package.bucket
  s3_key = aws_s3_object.summarise_deployment_package.key
  runtime       = "java11"
  handler       = "function.summarise::handleRequest"
  source_code_hash = filebase64sha256(aws_s3_object.summarise_deployment_package.source)
  role          = data.aws_iam_role.existing.arn
  timeout = 900
  memory_size = 512
}

output "lambda_arn" {
  description = "The ARN of the Lambda function"
  value       = aws_lambda_function.summarise_function.arn
}

output "lambda_function_name" {
  description = "The name of the Lambda function"
  value       = aws_lambda_function.summarise_function.function_name
}