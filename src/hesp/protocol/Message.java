package hesp.protocol;

import jade.lang.acl.ACLMessage;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Message<T> {
    private Action action;
    private T value;
    
    public Message(Action action, T value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
    
    private static JsonObject readJson(ACLMessage message) {
        JsonParser parser = new JsonParser();
        JsonElement tree = parser.parse(message.getContent());
        JsonObject content = tree.getAsJsonObject();
        return content;
    }
    
    public static Action getAction(ACLMessage message) {
        JsonObject content = readJson(message);
        Gson gson = new Gson();
        Action action = gson.fromJson(content.get("action"), Action.class);
        return action;
    }

    public static <T> Message<T> decode(ACLMessage message, Class<T> clazz) {
        JsonObject content = readJson(message);
        Gson gson = new Gson();
        Action action = gson.fromJson(content.get("action"), Action.class);
        T object = gson.fromJson(content.get("object"), clazz);
        
        return new Message<>(action, object);
    }
    
    
}
