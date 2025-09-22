# TODO: update function location and use java 11
variable "id" {}
variable "region" {}
variable "endpoint_name" {}
variable "processor_name" {}

data "aws_iam_role" "existing" {
  name = "LabRole"
}

resource "aws_s3_bucket" "deployment_bucket" {
  bucket        = "deployment-packages-${var.region}-${var.id}"
  force_destroy = true
}

resource "aws_s3_object" "split_deployment_package" {
  bucket = aws_s3_bucket.deployment_bucket.id
  key    = "split.jar"
  acl    = "private"  # or can be "public-read"
  source = "${path.root}/../../DAECCProject/split/target/deployable/split-1.0-SNAPSHOT.jar"
  etag   = filemd5("${path.root}/../../DAECCProject/split/target/deployable/split-1.0-SNAPSHOT.jar")
}

resource "aws_s3_object" "summarise_deployment_package" {
  bucket = aws_s3_bucket.deployment_bucket.id
  key    = "summarize.jar"
  acl    = "private"  # or can be "public-read"
  source = "${path.root}/../../DAECCProject/summarise/target/deployable/summarise-1.0-SNAPSHOT.jar"
  etag   = filemd5("${path.root}/../../DAECCProject/summarise/target/deployable/summarise-1.0-SNAPSHOT.jar")
}

resource "aws_s3_object" "merge_deployment_package" {
  bucket = aws_s3_bucket.deployment_bucket.id
  key    = "merge.jar"
  acl    = "private"  # or can be "public-read"
  source = "${path.root}/../../DAECCProject/merge/target/deployable/merge-1.0-SNAPSHOT.jar"
  etag   = filemd5("${path.root}/../../DAECCProject/merge/target/deployable/merge-1.0-SNAPSHOT.jar")
}

resource "aws_lambda_function" "summarise_function" {
  function_name = "summarise_${var.region}"
  s3_bucket = aws_s3_object.summarise_deployment_package.bucket
  s3_key = aws_s3_object.summarise_deployment_package.key
  runtime       = "java17"
  handler       = "function.summarise::handleRequest"
  source_code_hash = filebase64sha256(aws_s3_object.summarise_deployment_package.source)
  role          = data.aws_iam_role.existing.arn
  timeout = 900
  memory_size = 512

  environment {
    variables = {
      ENDPOINT_NAME = var.endpoint_name
      PROCESSOR_NAME = var.processor_name
    }
  }
}

resource "aws_lambda_function" "split_function" {
  function_name = "split_${var.region}"
  s3_bucket = aws_s3_object.split_deployment_package.bucket
  s3_key = aws_s3_object.split_deployment_package.key
  runtime       = "java17"
  handler       = "function.SplitFunction::handleRequest"
  source_code_hash = filebase64sha256(aws_s3_object.split_deployment_package.source)
  role          = data.aws_iam_role.existing.arn
  timeout = 500
  memory_size = 512
}

resource "aws_lambda_function" "merge_function" {
  function_name = "merge_${var.region}"
  s3_bucket = aws_s3_object.merge_deployment_package.bucket
  s3_key = aws_s3_object.merge_deployment_package.key
  runtime       = "java17"
  handler       = "function.MergeFunction::handleRequest"
  source_code_hash = filebase64sha256(aws_s3_object.merge_deployment_package.source)
  role          = data.aws_iam_role.existing.arn
  timeout = 500
  memory_size = 512
}

output "summarise_arn" {
  value       = aws_lambda_function.summarise_function.arn
}

output "split_arn" {
  value       = aws_lambda_function.split_function.arn
}

output "merge_arn" {
  value       = aws_lambda_function.merge_function.arn
}