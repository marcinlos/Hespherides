package hesp.agents;

import hesp.gui.SocialWindow;
import hesp.gui.Synchronous;
import hesp.protocol.Action;
import hesp.protocol.Job;
import hesp.protocol.JobReport;
import hesp.protocol.JobRequestResponse;
import hesp.protocol.Message;
import hesp.util.LogSink;
import jade.core.AID;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class SocialAgent extends HespAgent {
    
    private String name;
    
    private List<AID> ownedPA = new ArrayList<>();
    private List<AID> purchase = new ArrayList<>();
    private List<AID> pub = new ArrayList<>();
    
    private int endownment = 100;
    
    private SocialWindow window;
    private LogSink logger;
    
    
    @Override
    protected boolean dispatchMessage(ACLMessage message) {
        Message<?> content = Message.decode(message, Object.class);
        Action action = content.getAction();
        switch (action.category()) {
        case CONTROL: 
            //addBehaviour(new ControlProcessor(message));
            return true;
        case JOB:
            addBehaviour(new JobSubmissionProcessor(message));
            return true;
        default:
            return false;
        }
    }

    
    /**
     * Asynchronous job submission processor
     */
    private class JobSubmissionProcessor extends FSMBehaviour {
        
        private static final String ACCEPTANCE = "Acceptance";
        private static final String POLICY_MAPPING = "Policy mapping";
        private static final String POLICY_ENFORCING = "Policy enforcing";
        private static final String REJECT = "Reject";
        private static final String DISCOVERY = "Discovery";
        private static final String EXECUTION = "Execution";
        private static final String EXEC_SUCCESS = "Exec success";
        private static final String EXEC_FAILURE = "Exec failure";
        private static final String BILLING = "Billing";
        
        /** Default behaviour returns 0 */
        private static final int OK = 0;
        private static final int FAIL = 1;
        
        /** 
         * Key to data store associated with string describing the reason
         * of refusal.
         */
        private static final int KEY_FAIL_REASON = 1234;
        
        private Job job;
        private AID sender;
        private JobReport report;
        
        private DataStore DS = getDataStore();
        
        public JobSubmissionProcessor(final ACLMessage firstMessage) {

            registerTransition(ACCEPTANCE, POLICY_MAPPING, OK);
            registerTransition(POLICY_MAPPING, POLICY_ENFORCING, OK);
            registerTransition(POLICY_ENFORCING, REJECT, FAIL);
            registerTransition(POLICY_ENFORCING, DISCOVERY, OK);
            registerTransition(DISCOVERY, EXECUTION, OK);
            registerTransition(EXECUTION, EXEC_FAILURE, FAIL);
            registerTransition(EXECUTION, EXEC_SUCCESS, OK);
            registerTransition(EXEC_FAILURE, BILLING, OK);
            registerTransition(EXEC_SUCCESS, BILLING, OK);
            
          //-------------------------------------------------------
            // Indentation smaller for clarity
            //-------------------------------------------------------
            
    registerFirstState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("Acceptance");
        }
    }, ACCEPTANCE);
    
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("Policy mapping");
        }
    }, POLICY_MAPPING);
    
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("Policy enforcing");
        }
        @Override public int onEnd() { 
            DS.put(KEY_FAIL_REASON, "Too many queued jobs");
            return Math.random() > 0.5 ? OK : FAIL; 
        }
    }, POLICY_ENFORCING);
    
    registerLastState(new OneShotBehaviour() {
        @Override public void action() {
            ACLMessage reply = firstMessage.createReply();
            String reason = (String) DS.get(KEY_FAIL_REASON);
            JobRequestResponse resp = 
                    new JobRequestResponse(job.getId(), false, reason);
            sendMessage(reply, Action.JOB_COMPLETED, resp);
        }
    }, REJECT);
    
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            final AID aid = new AID("Res1", AID.ISLOCALNAME);
            LinkSupervisionMaster master = new LinkSupervisionMaster(SocialAgent.this, aid, firstMessage.getConversationId()) {
                
                @Override
                protected int slaveTimeout() {
                    // TODO Auto-generated method stub
                    return FAIL;
                }
                
                @Override
                protected int finished() {
                    // TODO Auto-generated method stub
                    return OK;
                }
            };
            registerState(master, EXECUTION);
        }
    }, DISCOVERY);
    
    registerState(new OneShotBehaviour() {
        @Override
        public void action() {
        }
    }, EXECUTION);
    
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            logger.error("Execution failure");
            ACLMessage reply = firstMessage.createReply();
            sendMessage(reply, Action.JOB_COMPLETED, report);
        }
    }, EXEC_FAILURE);
    
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            ACLMessage reply = firstMessage.createReply();
            sendMessage(reply, Action.JOB_COMPLETED, report);
        }
    }, EXEC_SUCCESS);
    
    registerLastState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("It's pay time!");
        }
    }, BILLING);
    
            //-------------------------------------------------------
            // Smaller indentation ends
            //-------------------------------------------------------
    
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
