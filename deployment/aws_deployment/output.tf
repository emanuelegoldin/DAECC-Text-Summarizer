##############################################################################################
#                                       AWS Outputs                                          #
##############################################################################################

output "summarise_arn_us_east_1" {
  description = "The ARN of the summarise function"
  value       = module.lambda_us_east_1.summarise_arn
}

output "split_arn_us_east_1" {
  description = "The ARN of the split function"
  value       = module.lambda_us_east_1.split_arn
}

output "merge_arn_us_east_1" {
  description = "The ARN of the merge function"
  value       = module.lambda_us_east_1.merge_arn
}

# output "summarise_arn_us_west_2" {
#   description = "The ARN of the Lambda function"
#   value       = module.lambda_us_west_2.summarise_arn
# }

# output "split_arn_us_west_2" {
#   description = "The ARN of the Lambda function"
#   value       = module.lambda_us_west_2.split_arn
# }

# output "merge_arn_us_west_2" {
#   description = "The ARN of the Lambda function"
#   value       = module.lambda_us_west_2.merge_arn
# }

# output "api_gateway_url_us_west_2" {
#   description = "The URL of the API Gateway"
#   value       = module.api_gateway_us_west_2.api_gateway_url
# }

# output "api_gateway_url_us_east_1" {
#   description = "The URL of the API Gateway"
#   value       = module.api_gateway_us_east_1.api_gateway_url
# }