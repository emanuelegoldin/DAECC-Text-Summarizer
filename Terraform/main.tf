module "lambda_us_west_2" {
  source = "./lambda"
  
  region = "us-west-2"
  function_name = "lambda_function_us_west_2"
}

module "lambda_us_east_1" {
  source = "./lambda"
  
  region = "us-east-1"
  function_name = "lambda_function_us_east_1"
}