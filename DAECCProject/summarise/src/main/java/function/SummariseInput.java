package function;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SummariseInput {
    String provider;
    String inputType;
    String inputFile;
    String inputBucket;
    String outputBucket;
}
