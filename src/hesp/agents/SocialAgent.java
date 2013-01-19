package hesp.agents;

import hesp.gui.SocialWindow;
import hesp.gui.Synchronous;
import hesp.protocol.Action;
import hesp.protocol.Message;
import hesp.util.LogSink;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class SocialAgent extends HespAgent {
    
    private String name;
    
    private class ResourceManager {
        
        private List<AID> ownedPA = new ArrayList<>();
        private List<AID> purchase = new ArrayList<>();
        private List<AID> pub = new ArrayList<>();

        
    }
    
    private int endownment = 100;
    
    private SocialWindow window;
    private LogSink logger;
    
    interface Accessor {
        LogSink getLogger();
    }
    
    private Accessor accessor = new Accessor() {
        
        @Override
        public LogSink getLogger() {
            return logger;
        }
    };
    
    @Override
    protected boolean dispatchMessage(ACLMessage message) {
        Message<?> content = Message.decode(message, Object.class);
        Action action = content.getAction();
        switch (action.category()) {
        case CONTROL: 
            //addBehaviour(new ControlProcessor(message));
            return true;
        case JOB:
            addBehaviour(new SocialJobProcessor(this, accessor, message));
            return true;
        default:
            return false;
        }
    }
    
    @Override
    public void setup() {
        super.setup();
        name = getLocalName();
        setupGUI();
        System.out.println("SGA '" + name + "' created");
    }
    
    private void setupGUI() {
        Synchronous.invoke(new Runnable() {
            @Override
            public void run() {
                window = new SocialWindow(SocialAgent.this);
                window.pack();
                window.setVisible(true);
                logger = window.getLogger().logSink();
            }
        });
    }

    @Override
    public void takeDown() {
        System.out.println("SGA '" + name + "' shutting down");
    }

}
