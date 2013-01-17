package hesp.agents;

import hesp.protocol.Action;
import hesp.protocol.Message;
import hesp.protocol.Protocols;
import jade.content.ContentManager;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class HespAgent extends Agent {
    
    private String cidBase;
    private Random rand = new Random();
    
    protected static final String IN_REPLY_TO_INIT = "<init>";

    
    protected String genCID() {
        if (cidBase == null) {
            cidBase = getLocalName() + hashCode() + "_" +
                    System.currentTimeMillis() % 10000;
        }
        return cidBase + rand.nextInt(Integer.MAX_VALUE);
    }
    
    
    private class Initiator extends CyclicBehaviour {

        private final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(Protocols.GRID),
                MessageTemplate.MatchInReplyTo(IN_REPLY_TO_INIT));

        @Override
        public void action() {
            ACLMessage message = receive(template);
            if (message != null) {
                boolean understood = dispatchMessage(message);
                if (! understood) {
                    // Send NOT UNDERSTOOD response
                    ACLMessage response = message.createReply();
                    response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    send(response);
                }
            } else {
                block();
            }
        }
        
    }
    
    protected abstract boolean dispatchMessage(ACLMessage message);
    
    
    protected ACLMessage emptyMessage(int performative) {
        ACLMessage message = new ACLMessage(performative);
        message.setProtocol(Protocols.GRID);
        message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        message.setSender(getAID());
        return message;
    }
    
    
    protected ACLMessage emptyInitMessage(int performative) {
        ACLMessage message = emptyMessage(performative);
        message.setConversationId(genCID());
        message.setInReplyTo(IN_REPLY_TO_INIT);
        return message;
    }
    
    
    protected void sendMessage(ACLMessage template, String content) {
        template.setContent(content);
        send(template);
    }
    
    
    protected void sendMessage(ACLMessage template, JsonObject json) {
        sendMessage(template, json.toString());
    }
    
    
    protected <T> void sendMessage(ACLMessage template, Action action, 
            T object) {
        Gson gson = new Gson();
        JsonObject content = new JsonObject();
        content.add("action", gson.toJsonTree(action));
        content.add("object", gson.toJsonTree(object));
        sendMessage(template, content.toString());
    }
    
    
    protected <T> void sendMessage(ACLMessage template, Message<T> message) {
        sendMessage(template, message.getAction(), message.getValue());
    }
    
    
    protected <T> Message<T> decode(ACLMessage message, Class<T> clazz) {
        JsonParser parser = new JsonParser();
        JsonElement tree = parser.parse(message.getContent());
        JsonObject content = tree.getAsJsonObject();
        
        Gson gson = new Gson();
        Action action = gson.fromJson(content.get("action"), Action.class);
        T object = gson.fromJson(content.get("object"), clazz);
        
        return new Message<>(action, object);
    }
    
    
    @Override
    public void setup() {
        try {
            ContentManager cm = getContentManager();
            cm.registerLanguage(new SLCodec());
            addBehaviour(new Initiator());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
