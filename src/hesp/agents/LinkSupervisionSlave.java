package hesp.agents;

import hesp.protocol.Action;
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
public abstract class LinkSupervisionSlave extends Behaviour {

    private boolean running = true;
    private int end;
    
    private String cid;
    private HespAgent slave;
    private AID master;
    
    public LinkSupervisionSlave(AID master, HespAgent slave, String cid) {
        this.master = master;
        this.slave = slave;
        this.cid = cid;
    }
    
    private void sendAck(ACLMessage beat) {
        ACLMessage ack = beat.createReply();
        slave.sendMessage(ack, Action.LS_ACK, null);
    }
    
    @Override
    public void action() {
       MessageTemplate template = MessageTemplate.MatchConversationId(cid);
        ACLMessage beat = slave.receive(template);
        
        if (beat != null) {
            Message<?> msg = slave.decode(beat, Object.class);
            switch (msg.getAction()) {
            case LS_BEAT:
                sendAck(beat);
                break;
            }
        }
    }

    @Override
    public boolean done() {
        return !running;
    }
    
    @Override
    public int onEnd() {
        return end;
    }
    
    public void stop(int code) {
        end = code;
        running = false;
        
        ACLMessage ack = slave.emptyMessage(ACLMessage.INFORM);
        ack.setConversationId(cid);
        ack.addReceiver(master);
        slave.sendMessage(ack, Action.LS_ACK, null);
    }

    protected abstract int masterTimeout();
    
}
