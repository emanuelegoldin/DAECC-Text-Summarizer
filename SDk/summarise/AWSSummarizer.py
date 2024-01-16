import summarise_service
from boto3 import client

ENDPOINT_NAME = "summarizer-endpoint"

class aws_summary_service(summarise_service.SummarizeService):
    def __init__(self):
        self.summary_service = summarise_service.SummarizeService()

    def get_summary(self, text) -> str:
        runtime = client('runtime.sagemaker', region_name='us-east-1')

        response = runtime.invoke_endpoint(EndpointName=ENDPOINT_NAME,
                                       ContentType='application/x-text',
                                       Body=text.encode("utf-8"))
        # Do something
        return ""