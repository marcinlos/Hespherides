package hesp.agents;

import com.google.gson.JsonSyntaxException;

import hesp.protocol.Action;
import hesp.protocol.Message;
import hesp.protocol.Action.Category;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * One of two classes responsible for Link Supervision Protocol.
 * <p>
 * It implements the Master side of the protocol - it periodically sends
 * keepalive messages to the Slave, and expects to receive an ACK.
 * 
 * @author marcinlos
 * @see LinkSupervisionSlave
 * 
 */
public abstract class LinkSupervisionMaster extends ParallelBehaviour {
    
    private int beatTime = 2000;
    //private int beatTimeout = 500;
    
    // Max missed beats
    private int treshold = 3;

    private boolean running = true;
    private int exitStatus;
    
    private HespAgent master;
    private AID slave;
    private String cid;
    
    public LinkSupervisionMaster(final HespAgent master, final AID slave, 
            final String cid) {
        this.master = master;
        this.slave = slave;
        this.cid = cid;
        
        this.addSubBehaviour(new TickerBehaviour(this.master, beatTime) {
            @Override
            protected void onTick() {
                if (running) {
                    sendBeat();
                } else {
                    stop();
                }
            }
        });
        
        this.addSubBehaviour(new Behaviour() {
            
            private long lastAck = System.currentTimeMillis();
            
            @Override
            public void action() {
                MessageTemplate template = MessageTemplate.and(
                        MessageTemplate.MatchConversationId(cid),
                        Action.MatchCategory(Category.LINK_SUPERVISION));
                ACLMessage ack = master.receive(template);
                if (ack != null) {
                    try {
                    Message<?> msg = Message.decode(ack, Object.class);
                    switch (msg.getAction()) {
                    case LS_ACK:
                        lastAck = System.currentTimeMillis();
                        break;
                    case LS_END:
                        stopProtocol();
                        exitStatus = finished();
                        break;
                    }
                    } catch (JsonSyntaxException e) {
                        exitStatus = handleNotUnderstood(ack);
                    }
                } else {
                    long ackTime = System.currentTimeMillis();
                    long elapsed = ackTime - lastAck;
                    if (elapsed > treshold * beatTime) {
                        // Timeout
                        exitStatus = slaveTimeout();
                        stopProtocol();
                    }
                }
            }
            
            @Override
            public boolean done() {
                return !running;
            }
            
        });
    }
    
    protected void stopProtocol() {
        running = false;
    }
    
    public int getExitStatus() {
        return exitStatus;
    }
    
    public void setExitStatus(int code) {
        exitStatus = code;
    }
    
    private void sendBeat() {
        ACLMessage message = master.emptyMessage(ACLMessage.REQUEST);
        message.setConversationId(cid);
        message.addReceiver(slave);
        master.sendMessage(message, Action.LS_BEAT, null);
    }
    
    @Override 
    public int onEnd() {
        return exitStatus;
    }
    
    /**
     * Invoked when the protocol has orderely shut down.
     * 
     * @return exit code for this behaviour, i.e. value returned by 
     * {@link #onEnd()}
     */
    protected abstract int finished();
    
    /**
     * Invoked when the supervisor has not received 3 consecutive ACK messages
     * from the slave.
     * 
     * @return exit code for this behaviour, i.e. value returned by 
     * {@link #onEnd()}
     */
    protected abstract int slaveTimeout();
    
    protected int handleNotUnderstood(ACLMessage message) {
        return 0;
    }

}
