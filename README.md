# Federated BaaS: Summarizing Service - 02.2024

There are already existing solutions to manipulate textual content leveraging services offered by cloud providers, such as pipelines to translate a text into different languages or to convert it to another format to ease its fruition (e.g., text-to-speech conversion).
Something that seems missing is a summarizing service that can help deal with a high volume of information. As we will cover in more detail further in the report, one of the reasons why this is missing is probably the lack of out-of-the-box services to perform this operation.
The objective of our Federated Summarizing Service is not only to provide this missing solution, but to make it interoperable and easy to integrate into already existing pipelines.

The principal outcome of our project is a reusable SDK: with a custom SDK we will be able to dynamically select the best combination of services on the supported providers to reduce costs and execution time.
One thing we are not focusing on in this project is the reliability of the summary produced by the service since this is mostly related to the underlying mechanism utilized (e.g., which model we use to extract data from a text).
These kinds of details will realistically be of the final user concerns: we aim to provide the infrastructure to summarise a high volume of data; how the user wants to actually summarise it, is out of the scope of our project.

For technical information regarding **usage** and **deployment**, please refer to the _Documentation_ folder.

## Use Cases

### Scenario 1

The primary user is a professional who has to deal with vast amounts of textual information on a regular basis.
This user values **time efficiency** and seeks a quick understanding of the key points within a document without delving into its full contents.
Imagine a business professional who receives numerous reports, research papers, and articles daily from various sources such as emails, shared drives, and online platforms.
The user wants to make well-informed decisions, but is often constrained by time.

Key requirements inferred from this scenario are:

- (**R1**) The user shall be able to work with an arbitrary number of documents
- (**R2**) The user wants to avoid vendor lock
- (**R3**) The user shall be able to seamlessly switch to the provider offering the best performance (considering aspects as latency, geographical location of data, bandwidth, costs per request, etc.)

### Scenario 2 

Imagine a scenario where a developer is building applications for multiple platforms, all of which require text summarization capabilities.
With the existing solutions, developers need a huge development effort to move from one service provider to another.

Key requirements inferred from this scenario are:
- (**R4**) The developer shall switch between different providers **without** modifying the source code of the application
- (**R5**) The developer shall be able to integrate our solution in other projects effortlessly (reusability)

## Proposed Solution

Our federated SaaS (Summarization as a Service) will work on both AWS (Amazon Web Services) and GCP (Google Cloud Provider) and can be easily extended to support different providers, dealing with the concerns of avoiding vendor-lock solution (**R2**).
Here is a picture of the architecture of our solution:

<img width="799" height="534" alt="image" src="https://github.com/user-attachments/assets/9120bbb6-1d76-4704-813c-0c653716cbf9" />

### SDK

We developed a custom library to abstract the configuration necessary to interact with multiple providers (AWS and GCP in our case) to address **R5**. 
The SDK structure is summarized in the following class diagram:

<img width="952" height="424" alt="image" src="https://github.com/user-attachments/assets/24286de2-2648-474f-895f-160890eda6eb" />

The library embeds both AWS and GCP SDKs, enabling deployment of the same function to both providers without the need to change the implementation (**R4**).
There are two core components: the abstraction of the service, represented by the _SummarizeService_ interface, and the function to be deployed, namely the _ServerlessFunction_ in our diagram.

The SummarizeService interface defines the contract the service will observe through the exposed summarise method: given the location of a file as an input, it will provide a _SummarizerResponse_ as an output containing the summary of the text.
We provide two concrete implementations based on this interface to enable interoperability between AWS and GCP.
To achieve this, we leveraged the [CORE](https://github.com/FaaSTools/CORE/tree/main) modules to manage storage access and credentials sharing.

A factory will generate the concrete summarizer based on the provided heuristics (in the current state, the desired provider to execute the service).
Through the factory, the ServerlessFunction can work with the abstract representation of the service. To be able to execute the same function on every
supported provider, the ServerlessFunction implements the necessary handler methods (i.e., the handleRequest for AWS lambdas and service for GCP cloud functions), addressing **R3**.

### Workflow

The complete workflow of our application consists of three types of functions:

- **Split**: Takes a file location as input, loads it locally, and splits it into pages. The pages are then stored in a bucket, and a collection of URLs of those pages is generated as output.
- **Summarise**: Receives the location of a file as input, loads it locally, and invokes the summarise service through our SDK. The summary will be stored in a bucket, and the URL pointing to the summary is returned as the output of the function.
- **Merge**: Receives a collection of URLs pointing to different files as input, loads them locally, and merges them into a single one. The resulting file is then stored in a bucket, and a URL pointing to it is returned as the output.

We now show the whole workflow defined as an [AFCL](https://github.com/sashkoristov/AFCL) diagram:

<img width="480" height="912" alt="image" src="https://github.com/user-attachments/assets/cebd08be-51ed-4cc9-8813-821d5462ea6d" />

The image shows the iteration for a single file. Wrapping it in a parallel loop allows the processing of an arbitrary number of files (**R1**).

## Evaluation

### Execution Time per Configuration

Executed 20 runs for each configuration and kept the average. (AMD Ryzen 7 PRO 5850U, 16 GB RAM)

<img width="996" height="220" alt="image" src="https://github.com/user-attachments/assets/0a74d337-c1d1-4e0c-83b2-9bacad499086" />

### Lines of Code

Naive manual creation of separate workflows for each supported provider compared to the use of our SDK (we do not count the amount of lines the SDK is composed of: we focus solely on what a user will have to write for a working serverless function)

<img width="510" height="105" alt="image" src="https://github.com/user-attachments/assets/9ec3c20f-e1b2-4259-9df2-382e6569649e" />

## Future Work

- Use layers to spare deployment time
- Provide an entry point for configuration
- Define additional heuristics
