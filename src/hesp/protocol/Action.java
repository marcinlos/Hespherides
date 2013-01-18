package hesp.protocol;

import com.google.gson.JsonSyntaxException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public enum Action {

    SET_OWNER(Category.CONTROL),
    SET_POLICY(Category.CONTROL),
    
    JOB_REQUEST(Category.JOB),
    JOB_SUBMITTED(Category.JOB),
    JOB_COMPLETED(Category.JOB),
    
    CREATE_ACCOUNT(Category.BANK),
    CREATE_ACCOUNT_ACK(Category.BANK),
    TRANSFER(Category.BANK),
    TRANSFER_ACK(Category.BANK),
    CHECK_ACC_STATE(Category.BANK),
    ACC_STATE(Category.BANK),
    
    LS_BEAT(Category.LINK_SUPERVISION),
    LS_ACK(Category.LINK_SUPERVISION),
    LS_END(Category.LINK_SUPERVISION);
    
    
    public enum Category {
        CONTROL, JOB, BANK, LINK_SUPERVISION
    }
    
    private final Category category;
    
    Action(Category category) {
        this.category = category;
    }
    
    public Category category() {
        return category;
    }
    
    public static MessageTemplate MatchAction(final Action a) {
        return new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                try {
                    Action action = Message.getAction(msg);
                    return action == a;
                } catch (JsonSyntaxException e) {
                    return false;
                }
            }
        });
    }
    
    public static MessageTemplate MatchCategory(final Category category) {
        return new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                try {
                    Action action = Message.getAction(msg);
                    return action.category() == category;
                } catch (JsonSyntaxException e) {
                    return false;
                }
            }
        });
    }
    
}
