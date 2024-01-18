package summarise_service;

public class Main {
    public static void main(String[] args) {
        SummarizerRequest request = new SummarizerRequest().getBuilder()
                .setInputFile("path/to/inputFile")
                .build();
        System.out.println(request.inputFile);
    }
}
