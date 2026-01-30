@RestController
@RequestMapping("/api")
public class ChatController {

    private final RAGService ragService;

    public ChatController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String question) {
        System.out.println("=== /chat called ===");
        System.out.println("Question received: " + question);

        String answer = ragService.getAnswer(question);

        System.out.println("Answer returned: " + answer);
        return answer;
    }
}
