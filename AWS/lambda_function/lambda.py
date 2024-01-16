import json
import dummy_module
import os
import io
import boto3
import json
from google.protobuf import documentai

ENDPOINT_NAME = "summarizer-endpoint"
runtime= boto3.client('runtime.sagemaker', region_name='us-east-1') # We deploy a single model to us-east-1

def lambda_handler(event, context):
    data = json.loads(event['body'])
    payload = data['input']
    
    encoded_text = payload.encode("utf-8")

    # Invoke the SageMaker endpoint
    response = runtime.invoke_endpoint(EndpointName=ENDPOINT_NAME,
                                       ContentType='application/x-text',
                                       Body=encoded_text)

    # Unpack response
    responseBody = response['Body'].read()
    result = json.loads(responseBody)

    # Return the summary
    summary = result['summary_text']
    return {
        "statusCode": 200,
        "headers": { "Content-Type": "application/json" },
        "body": json.dumps({ "summary": summary })
    }
