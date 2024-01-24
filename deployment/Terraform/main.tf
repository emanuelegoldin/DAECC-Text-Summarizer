##########################################################################################################
#                                               AWS                                                      #
##########################################################################################################

module "lambda_us_west_2" {
  source = "./AWS/lambda"
  id = random_id.stack_id.hex
  region = "us-west-2"
  function_name = "lambda_function_us_west_2"
}

module "lambda_us_east_1" {
  source = "./AWS/lambda"
  id = random_id.stack_id.hex
  region = "us-east-1"
  function_name = "lambda_function_us_east_1"
}

module "sagemaker" {
  source = "./AWS/sagemaker"
  region = "us-east-1"
  model_name = "huggingface-summarization-distilbart-cnn-6-6"
  image = "763104351884.dkr.ecr.us-east-1.amazonaws.com/huggingface-pytorch-inference:1.13.1-transformers4.26.0-gpu-py39-cu117-ubuntu20.04"
  model_data_url = "s3://jumpstart-cache-prod-us-east-1/huggingface-infer/prepack/v1.0.0/infer-prepack-huggingface-summarization-distilbart-cnn-6-6.tar.gz"
}

module "api_gateway_us_east_1" {
  source = "./AWS/api_gateway"
  region = "us-east-1"
  lambda_arn = module.lambda_us_east_1.lambda_arn
  lambda_function_name = module.lambda_us_east_1.lambda_function_name
}

module "api_gateway_us_west_2" {
  source = "./AWS/api_gateway"
  region = "us-west-2"
  lambda_arn = module.lambda_us_west_2.lambda_arn
  lambda_function_name = module.lambda_us_west_2.lambda_function_name
}

##########################################################################################################
#                                               GCP                                                      #
##########################################################################################################

locals {
  credentials = jsondecode(file("./gcp_key.json"))
}

provider "google" {
  project = "summarization-project-406313"
  region  = "us-central1"
}

terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 4.34.0"
    }
  }
}



resource "google_storage_bucket" "function_bucket" {
  name     = "${random_id.function_bucket.hex}-function-bucket"
  location = "us-central1"
  uniform_bucket_level_access = true

}


resource "google_storage_bucket" "documents_bucket" {
  name     = "${random_id.documents_bucket.hex}-documents-bucket"
  location = "us-central1"
  uniform_bucket_level_access = true

}



# DocumentAI processor setup
resource "google_document_ai_processor" "summarizer_processor" {
  display_name = "summarizer-processor"
  type         = "SUMMARY_PROCESSOR"
  location     = "us"
}



module "java-function" {
  source = "./GCP/java-cloud-func"
  function-handler = "service"
  function_name = "java-summarizer"
  google_storage_bucket_name = google_storage_bucket.function_bucket.name
  region = "us-central1"
}



##########################################################################################################
#                                               Utils                                                    #
##########################################################################################################

resource "random_id" "stack_id" {
  byte_length = 8
}

resource "random_id" "function_bucket" {
  byte_length = 8
}
resource "random_id" "documents_bucket" {
  byte_length = 8
}

