package hesp.agents;

import com.google.gson.JsonSyntaxException;

import hesp.protocol.Action;
import hesp.protocol.Action.Category;
import hesp.protocol.Message;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * One of two classes implementing Link Supervision Protocol.
 * <p>
 * It implements the slave side of the protocol - it sends ack messages after
 * each beat received from the master. It sends LS_END message.
 * 
 * @author marcinlos
 * @see LinkSupervisionMaster
 */
public class LinkSupervisionSlave extends Behaviour {

    private boolean running = true;
    private int exitStatus;
    
    private String cid;
    private HespAgent slave;
    private AID master;
    
    public LinkSupervisionSlave(AID master, HespAgent slave, String cid) {
        this.master = master;
        this.slave = slave;
        this.cid = cid;
    }
    
    private void sendAck(ACLMessage beat) {
        System.out.println("Ack");
        ACLMessage ack = beat.createReply();
        slave.sendMessage(ack, Action.LS_ACK, null);
    }
    
    @Override
    public void action() {
       MessageTemplate template = MessageTemplate.and(
               MessageTemplate.MatchConversationId(cid),
               Action.MatchCategory(Category.LINK_SUPERVISION));
       
        ACLMessage beat = slave.receive(template);
        
        if (beat != null) {
            try {
                Message<?> msg = Message.decode(beat, Object.class);
                switch (msg.getAction()) {
                case LS_BEAT:
                    sendAck(beat);
                    break;
                }
            } catch (JsonSyntaxException e) {
                handleNotUnderstood(beat);
            }
        }
    }
    
    public int getExitStatus() {
        return exitStatus;
    }
    
    public void setExitStatus(int code) {
        exitStatus = code;
    }

    @Override
    public boolean done() {
        return !running;
    }
    
    @Override
    public int onEnd() {
        return exitStatus;
    }
    
    public void stop(int code) {
        exitStatus = code;
        running = false;
        
        ACLMessage ack = slave.emptyMessage(ACLMessage.INFORM);
        ack.setConversationId(cid);
        ack.addReceiver(master);
        slave.sendMessage(ack, Action.LS_END, null);
    }

    protected int masterTimeout() {
        return 0;
    };
    
    protected int handleNotUnderstood(ACLMessage message) {
        return 0;
    }
    
}
