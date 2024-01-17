package org.example;

class SummarizerFactory {

    public static SummarizeService Create(Provider provider){
        switch (provider){
            case AWS: return new AWSSummarizer();
            case GCP: return new GCPSummarizer();
            default: return null;
        }
    }
}
