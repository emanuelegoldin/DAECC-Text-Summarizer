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

resource "google_storage_bucket" "processed_documents_bucket" {
  name     = "${random_id.processed_documents_bucket.hex}-summarized-documents-bucket"
  location = "us-central1"
  uniform_bucket_level_access = true

}


# DocumentAI processor setup
resource "google_document_ai_processor" "summarizer_processor" {
  //project = local.credentials.project_id
  display_name = "summarizer-processor"
  type         = "SUMMARY_PROCESSOR"
  location     = "us"
}

module "bucket-triggered-function" {
  source = "./GCP/bucket-triggered-cloud-function"
  function-handler = "main"
  function_name = "read-from-bucket"
  google_storage_bucket_name = google_storage_bucket.function_bucket.name
  region = "us-central1"
  bucket-to-subscribe = google_storage_bucket.documents_bucket.name
  output-bucket-name = google_storage_bucket.processed_documents_bucket.name
  summarizer-processor-id = google_document_ai_processor.summarizer_processor.id
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