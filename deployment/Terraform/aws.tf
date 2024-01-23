resource "random_id" "stack_id" {
  byte_length = 8
}

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