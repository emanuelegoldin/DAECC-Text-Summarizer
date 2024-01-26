## Input

```json
{
  "inputFile": "sample.pdf",

  "inputBucket": "",
  "splitBucket": "",
  "extractBucket": "",

  "summariseBucket1": "",
  "summariseBucket2": "",
  
  "outputBucket": "",
  
  "summariseProvider1": "AWS",
  "summariseProvider2": "GCP"
}
```

### Split function

Input
```json
{
    "inputFile": "",
    "inputBucket" : "",
    "outputBucket" : ""
}
```

Output
```json
{
    "files": [],
    "filesCount": 0
}
```

### Summarise funciton

Input
```json
{
    "provider": "",
    "inputType": "",
    "inputFile": "",
    "inputBucket": "",
    "outputBucket": "",
}
```

From workflow input:
- provider (either AWS or GCP)
- inputType (either PLAIN or PDF)
- outputBucket

From split ouput:
- inputFile (file's url)
- inputBucket (split's outputBucket)

Output
```json

```

### Merge function

Input
```json
{
    "inputFiles": [],
    "outputBucket": "",
}
```

Output
```json
{
    "outputFile": ""
}
```

