package function;

public class SummariseOutput {
    String summary;

    public SummariseOutput() {
    }

    public SummariseOutput summary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary= summary;
    }
}
