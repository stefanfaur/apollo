package ro.faur.apollo.libs.images.analyzer.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QwenRequest {
    private String model;
    private List<Message> messages;
    private double temperature;
    @JsonProperty("max_tokens")
    private int maxTokens;
    private boolean stream;

    public QwenRequest() {}

    public QwenRequest(String base64Image) {
        this.model = "qwen2-vl-7b-instruct";
        this.temperature = 0.7;
        this.maxTokens = 512;
        this.stream = false;
        this.messages = List.of(
                new Message("user", List.of(new Content("text", "What's in this image?"),
                        new Content("image_url", "data:image/jpeg;base64," + base64Image)))
        );
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public static class Message {
        private String role;
        private List<Content> content;

        public Message() {}

        public Message(String role, List<Content> content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public List<Content> getContent() {
            return content;
        }

        public void setContent(List<Content> content) {
            this.content = content;
        }
    }

    public static class Content {
        private String type;
        private String text;
        private ImageUrl image_url;

        public Content() {}

        public Content(String type, String value) {
            this.type = type;
            if (type.equals("text")) {
                this.text = value;
            } else if (type.equals("image_url")) {
                this.image_url = new ImageUrl(value);
            }
        }

        public static class ImageUrl {
            private String url;

            public ImageUrl() {}

            public ImageUrl(String url) {
                this.url = url;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public ImageUrl getImage_url() {
            return image_url;
        }

        public void setImage_url(ImageUrl image_url) {
            this.image_url = image_url;
        }
    }
}

