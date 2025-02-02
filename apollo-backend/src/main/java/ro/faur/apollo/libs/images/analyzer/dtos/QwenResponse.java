package ro.faur.apollo.libs.images.analyzer.dtos;

import java.util.List;

public class QwenResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    public String getMessageContent() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).message.content;
        }
        return "No description available";
    }

    public static class Choice {
        private int index;
        private String finish_reason;
        private Message message;

        public Choice(int index, String finish_reason, Message message) {
            this.index = index;
            this.finish_reason = finish_reason;
            this.message = message;
        }

        public Choice() {}

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getFinish_reason() {
            return finish_reason;
        }

        public void setFinish_reason(String finish_reason) {
            this.finish_reason = finish_reason;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}

