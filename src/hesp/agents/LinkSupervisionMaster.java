package hesp.agents;

import hesp.protocol.Action;
import hesp.protocol.Message;
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
    
    private int beatTime = 1000;
    //private int beatTimeout = 500;
    
    // Max missed beats
    private int treshold = 3;

    private boolean running = true;
    private int end;
    
    //private long lastBeatSent = System.currentTimeMillis();

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
                    //lastBeatSent = System.currentTimeMillis();
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
                MessageTemplate template = MessageTemplate.MatchConversationId(cid);
                ACLMessage ack = master.receive(template);
                if (ack != null) {
                    Message<?> msg = master.decode(ack, Object.class);
                    switch (msg.getAction()) {
                    case LS_ACK:
                        lastAck = System.currentTimeMillis();
                        break;
                    case LS_END:
                        running = false;
                        end = finished();
                        break;
                    }
                } else {
                    long ackTime = System.currentTimeMillis();
                    long elapsed = ackTime - lastAck;
                    if (elapsed > treshold * beatTime) {
                        // Timeout
                        end = slaveTimeout();
                    }
                }
            }
            
            @Override
            public boolean done() {
                return !running;
            }
            
        });
    }
    
    private void sendBeat() {
        ACLMessage message = master.emptyMessage(ACLMessage.REQUEST);
        message.setConversationId(cid);
        message.addReceiver(slave);
        master.sendMessage(message, Action.LS_BEAT, null);
    }
    
    @Override 
    public int onEnd() {
        return end;
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

}
