package function;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SummariseOutput {
    private String outputfile;
    private long timeToSummarise;
    private long totalExecutionTime;
    private double ratio;
    private String executedOnProvider;
    private String calledByProvider;
}
