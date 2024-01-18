import org.junit.jupiter.api.Test;

import summarise_service.AWSConfig;
import summarise_service.AWSSummarizer;

import static org.junit.jupiter.api.Assertions.*;

class AWSSummarizerTest {

    @Test
    void testReadConfig() {
        AWSSummarizer awsSummarizer = new AWSSummarizer(null);
        AWSConfig config = awsSummarizer.readConfig();

        assertNotNull(config);
        assertEquals("summarizer-endpoint", config.EndpointName);
        assertEquals("The quick brown fox jumped over the lazy dog.", config.Text);
        // Add more assertions to validate the properties of the config object
        // For example:
        // assertEquals("accessKey", config.getAccessKey());
        // assertEquals("secretKey", config.getSecretKey());
        // assertEquals("region", config.getRegion());
    }
}