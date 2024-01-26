package summarise_service;

public class SummarizerFactory {

    public static SummarizeService Create(Provider provider){
        switch (provider){
            case AWS: return new AWSSummarizer();
            case GCP: return new GCPSummarizer();
            default: return null;
        }
    }

    public static SummarizeService Create(FileType fileType){
        switch (fileType){
            case PLAIN: return new AWSSummarizer();
            case PDF: 
            default: return new GCPSummarizer();
        }
    }

    public static SummarizeService Create(String inputUrl){
        if (inputUrl.startsWith("https://s3.amazonaws.com/") || inputUrl.contains(".s3.amazonaws.com/")) {
            return new AWSSummarizer();
        } else if (inputUrl.startsWith("gs://")) {
            return new GCPSummarizer();
        } else {
            // Handle case where URL does not match any known patterns
            return null;
        }
    }

    public static SummarizeService Create(Provider provider, String inputUrl){
        if (provider != null) {
            if(!CompliantStorage(provider, inputUrl)){
                throw new RuntimeException("Trying to access non-compliant storage with provider "+ provider +" and url "+ inputUrl +".");
            }
            return Create(provider);
        } else {
            return Create(inputUrl);
        }
    }

    private static boolean CompliantStorage(Provider provider, String inputUrl){
        return provider == Provider.AWS && inputUrl.contains("s3.amazonaws.com") || provider == Provider.GCP && inputUrl.contains("gs://");
    }
}
