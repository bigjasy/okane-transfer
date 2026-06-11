package ma.ensam.okanetransfer.dto.chatbot;

public class ChatbotRequest {
    private String message;
    private String language;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
