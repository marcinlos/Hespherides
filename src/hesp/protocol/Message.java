package hesp.protocol;

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
    
    
}
