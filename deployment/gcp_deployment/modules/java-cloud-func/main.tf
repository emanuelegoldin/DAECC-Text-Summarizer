variable "function_name" {
  description = "The name of the Lambda function"
}

variable "region" {
  description = "The gcp region in which to deploy the cloud function"
}

variable "function-handler" {
  description = "path of the code to jar zip"
}

variable "google_storage_bucket_name" {
  description = "storage bucket name"
}

variable "processor_name" {
  description = "name of the processor"
}

data "archive_file" "default" {
  type        = "zip"
  output_path = "${path.module}/${var.function_name}.zip"
  source_dir  = "${path.root}/../../DAECCProject/summarise/target/deployable"
}


resource "google_storage_bucket_object" "object" {
  name   = "${var.function_name}.zip"
  bucket = var.google_storage_bucket_name
  source = data.archive_file.default.output_path # Add path to the zipped function source code
}


resource "google_cloudfunctions2_function" "default" {
  name        = var.function_name
  location    = var.region
  description = "gcp_function"

  build_config {
    runtime     = "java17"
    entry_point = "function.summarise"
    source {
      storage_source {
        bucket = var.google_storage_bucket_name
        object = google_storage_bucket_object.object.name
      }
    }
  }

  service_config {
    max_instance_count = 1
    available_memory   = "512M"
    timeout_seconds    = 120
    environment_variables = {
      PROCESSOR_NAME = var.processor_name
      ENDPOINT_NAME = "summarizer-endpoint"
    }
  }
}


resource "google_cloud_run_service_iam_member" "member" {
  location = google_cloudfunctions2_function.default.location
  service  = google_cloudfunctions2_function.default.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
