import json
import dummy_module

def lambda_handler(event, context):
    print("Received event: " + json.dumps(event, indent=2))
    
    # Create a response with dummy data for testing
    response = {
        "statusCode": 200,
        "headers": {
            "Content-Type": "application/json"
        },
        "body": json.dumps({
            "message": dummy_module.dummy_function()
        })
    }
    return response