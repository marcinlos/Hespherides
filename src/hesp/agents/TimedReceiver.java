package hesp.agents;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class TimedReceiver extends SimpleBehaviour {
    
    private long timeout;
    private long wakeup;
    private MessageTemplate template;
    private boolean finished = false;
    
    private int exitStatus = 0;
    
    
    public TimedReceiver(Agent a, long timeout, MessageTemplate tempate) {
        super(a);
        this.timeout = timeout;
        this.template = tempate;
    }
    
    @Override
    public void onStart() {
        wakeup = timeout < 0 ? Long.MAX_VALUE : 
            System.currentTimeMillis() + timeout;
    }

    @Override
    public void action() {
        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            boolean done = handleMessage(message);
            if (done) {
                finished = true;
            }
        } else {
            long time = System.currentTimeMillis();
            long remaining = wakeup - time;
            if (remaining > 0) {
                block(remaining);
            } else {
                finished = true;
                handleTimeout();
            }
        }
    }
    
    public void setExitStatus(int code) {
        exitStatus = code;
    }

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public void reset() {
        finished = false;
        super.reset();
    }
    
    public void reset(int timeout) {
        this.timeout = timeout;
    }
    
    @Override
    public int onEnd() {
        super.onEnd();
        return exitStatus;
    }

    
    protected abstract boolean handleMessage(ACLMessage message);
    
    protected void handleTimeout() {
        // Empty body
    }
}

