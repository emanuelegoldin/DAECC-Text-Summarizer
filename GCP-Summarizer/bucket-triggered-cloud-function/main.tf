#------ variables -----------

variable "function_name" {
  description = "The name of the Lambda function"
}

variable "region" {
  description = "The gcp region in which to deploy the cloud function"
}

variable "function-handler" {
  description = "path of the code to zip"
}

variable "google_storage_bucket_name" {
  description = "storage bucket name"
}

variable "bucket-to-subscribe" {
  description = "bucket topic to subscribe"
}

variable "output-bucket-name" {
  description = "bucket name where the output is stored"
}
variable "summarizer-processor-id" {
  description = "id of the processor to e called"
}


# --- zip python folder with code and requirements

data "archive_file" "default" {
  type        = "zip"
  output_path = "${path.module}/${var.function_name}.zip"
  source_dir  = "${path.module}/${var.function_name}/python"
}

# --- create google bucket storage object with zip file and store it into the bucket

resource "google_storage_bucket_object" "object" {
  name   = "${var.function_name}.zip"
  bucket = var.google_storage_bucket_name
  source = data.archive_file.default.output_path
}


resource "google_cloudfunctions2_function" "default" {
  name        = var.function_name
  location    = var.region
  description = "gcp_function"

  build_config {
    runtime     = "python310"
    entry_point = var.function-handler
    source {
      storage_source {
        bucket = var.google_storage_bucket_name
        object = google_storage_bucket_object.object.name
      }
    }
  }

  service_config {
    max_instance_count = 1
    available_memory   = "256Mi"
    timeout_seconds    = 60
    ingress_settings = "ALLOW_INTERNAL_ONLY"
    all_traffic_on_latest_revision = true
    service_account_email = "370392824615-compute@developer.gserviceaccount.com"
    environment_variables = {
      PROCESSOR_ID = var.summarizer-processor-id,
      OUTPUT_BUCKET_NAME = var.output-bucket-name
    }
  }


  # trigger the function when a new object is uploaded to the input bucket
  event_trigger {
    trigger_region        = "us-central1" # the trigger must be in the same location as the bucket
    event_type            = "google.cloud.storage.object.v1.finalized"
    #service_account_email = "370392824615-compute@developer.gserviceaccount.com"
    event_filters {
      attribute = "bucket"
      value = var.bucket-to-subscribe
    }
  }
}


resource "google_cloud_run_service_iam_member" "member" {
  location = google_cloudfunctions2_function.default.location
  service  = google_cloudfunctions2_function.default.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_project_iam_member" "gcs_pubsub_publishing" {
  project = "summarization-project-406313"
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:370392824615-compute@developer.gserviceaccount.com"
}

resource "google_project_iam_member" "gcs_pubsub_publishing_2" {
  project = "summarization-project-406313"
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:summarization-project-406313@appspot.gserviceaccount.com"
}

resource "google_project_iam_member" "gcs_pubsub_publishing_3" {
  project = "summarization-project-406313"
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:service-370392824615@gs-project-accounts.iam.gserviceaccount.com"
}


resource "google_project_iam_member" "invoking" {
  project    = "summarization-project-406313"
  role       = "roles/run.invoker"
  member     = "serviceAccount:370392824615-compute@developer.gserviceaccount.com"

}

resource "google_project_iam_member" "event_receiving" {
  project    = "summarization-project-406313"
  role       = "roles/eventarc.eventReceiver"
  member     = "serviceAccount:370392824615-compute@developer.gserviceaccount.com"
  depends_on = [google_project_iam_member.invoking]
}

resource "google_project_iam_member" "artifactregistry_reader" {
  project    = "summarization-project-406313"
  role       = "roles/artifactregistry.reader"
  member     = "serviceAccount:370392824615-compute@developer.gserviceaccount.com"
  depends_on = [google_project_iam_member.event_receiving]
}


output "function_uri" {
  value = google_cloudfunctions2_function.default.service_config[0].uri
}