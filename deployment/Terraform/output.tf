output "lambda_arn_us_west_2" {
  description = "The ARN of the Lambda function"
  value       = module.lambda_us_west_2.lambda_arn
}

output "lambda_arn_us_east_1" {
  description = "The ARN of the Lambda function"
  value       = module.lambda_us_east_1.lambda_arn
}

output "api_gateway_url_us_west_2" {
  description = "The URL of the API Gateway"
  value       = module.api_gateway_us_west_2.api_gateway_url
}

output "api_gateway_url_us_east_1" {
  description = "The URL of the API Gateway"
  value       = module.api_gateway_us_east_1.api_gateway_url
}