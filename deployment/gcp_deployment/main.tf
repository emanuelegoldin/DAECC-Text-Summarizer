locals {
  credentials = jsondecode(file("./gcp_key.json"))
}

provider "google" {
  credentials = file("./gcp_key.json")
  project     = local.credentials.project_id
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
  source = "./modules/java-cloud-func"
  function-handler = "service"
  function_name = "java-summarizer"
  google_storage_bucket_name = google_storage_bucket.function_bucket.name
  region = "us-central1"
  processor_name = google_document_ai_processor.summarizer_processor.id
}

resource "random_id" "function_bucket" {
  byte_length = 8
}
resource "random_id" "documents_bucket" {
  byte_length = 8
}

resource "random_id" "processed_documents_bucket" {
  byte_length = 8
}

output "processor_name" {
  value = google_document_ai_processor.summarizer_processor.id
}