## Before apply

### AWS

#### Credentials 

create a `aws_credentials` file based on the provided `aws_credentials.example` and fill it with the required information.

```
[default]
aws_access_key_id = <aws_access_key_id>
aws_secret_access_key = <aws_access_key>
aws_session_token = <aws_token>
```

Pu the file in the `deployment/Terraform/AWS` folder

Note: you can retrieve the session credential after aws console started with the command `cat .aws/credentials`

### GCP

#### Credentials

create a `gcp_key.json` file which contains the access key of a service account.

```json
{
  "type": "service_account",
  "project_id": "",
  "private_key_id": "",
  "private_key": "",
  "client_email": "",
  "client_id": "",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/crafty-tractor-401010%40appspot.gserviceaccount.com",
  "universe_domain": "googleapis.com"
}
```

Place the file in the `deployment/Terraform` folder.

#### Enable APIs and services

- DocumentAI
- Cloud Run

#### Set roles

- cloud run admin (invoker is then included)
- service account user