package hesp.agents;

import hesp.gui.ClientWindow;
import hesp.gui.Synchronous;
import hesp.protocol.Action;
import hesp.protocol.Job;
import hesp.protocol.JobReport;
import hesp.protocol.JobRequestResponse;
import hesp.protocol.Message;
import hesp.util.LogItem.Level;
import hesp.util.LogSink;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashSet;
import java.util.Set;

import javax.swing.event.EventListenerList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Agent initiating the job requests, controlled by the client of a system.
 *
 * @author marcinlos
 */
public class ClientAgent extends HespAgent {
    
    private ClientWindow window;   
    
    private LogSink logger;
    private ServiceLocator locator;
    private AID bank;
    private Set<AID> banks = new HashSet<AID>(); 
    
    /** Lists of client agent events listeners */
    private EventListenerList listeners = new EventListenerList();
    
    /**
     * Client currently does not receive messages outside the scope of his
     * own initiated conversations.
     */
    @Override
    protected boolean dispatchMessage(ACLMessage message) {
        // Empty body
        return false;
    }
    
    private class JobExecutor extends FSMBehaviour {
        
        private ACLMessage message;
        
        private static final String POST_JOB = "Post job";
        private static final String WAIT_FOR_RESP = "Wait for response";
        private static final String WAIT_FOR_RESULT = "Wait for result";
        private static final String REJECTED = "Rejected";
        
        private static final int OK = 0;
        private static final int FAIL = 1;
        
        public JobExecutor(final AID agent, final Job job) {
            //this.job = job;
            
            registerTransition(POST_JOB, WAIT_FOR_RESP, OK);
            registerTransition(WAIT_FOR_RESP, WAIT_FOR_RESULT, OK);
            registerTransition(WAIT_FOR_RESP, REJECTED, FAIL);

            //-------------------------------------------------------
            // Indentation smaller for clarity
            //-------------------------------------------------------
            
    // Initial state - we create initial message, fill it with details
    // of job to be requested, and send it to target agent
    registerFirstState(new OneShotBehaviour() {
        @Override
        public void action() {
            ACLMessage message = emptyInitMessage(ACLMessage.REQUEST);
            message.addReceiver(agent);
            sendMessage(message, Action.JOB_REQUEST, job);
            JobExecutor.this.message = message;
        }
    }, POST_JOB);
    
    // Wait for response for job request just posted
    registerState(new Behaviour() {
        private boolean run = true;
        // End state
        private int end = OK;
        
        @Override
        public void action() {
            String cid = message.getConversationId();
            MessageTemplate template = MessageTemplate.MatchConversationId(cid);
            
            // Wait for next message in this conversation
            ACLMessage reply = receive(template);
            if (reply != null) {
                Message<JobRequestResponse> msg = 
                        decode(reply, JobRequestResponse.class);
                JobRequestResponse resp = msg.getValue();
                end = resp.isAccepted() ? OK : FAIL;
                // callback
                requestProcessed(resp);
                run = false;
            } else {
                block();
            }
        }
        
        @Override 
        public boolean done() { 
            return !run; 
        }
        
        @Override
        public int onEnd() {
            return end;
        }
        
    }, WAIT_FOR_RESP);
    
    // Job request was rejected
    registerLastState(new OneShotBehaviour() {
        @Override
        public void action() {
            System.out.println("Job was rejected");
        }
    }, REJECTED);
    
    // Job was accepted, wait for results
    registerLastState(new Behaviour() {
        private boolean run = true;
        
        @Override
        public void action() {
            String cid = message.getConversationId();
            MessageTemplate template = MessageTemplate.MatchConversationId(cid);
            
            ACLMessage reply = receive(template);
            if (reply != null) {
                Message<JobReport> msg = decode(reply, JobReport.class);
                JobReport report = msg.getValue();
                jobCompleted(report);
                run = false;
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return !run;
        }
        
    }, WAIT_FOR_RESULT);
    
            //-------------------------------------------------------
            // Smaller indentation ends
            //-------------------------------------------------------
        }
    }
    
    /**
     * Registers new production events listener.
     * 
     * @param listener Listener to add to list
     */
    public void addListener(ClientAgentListener listener) {
        listeners.add(ClientAgentListener.class, listener);
    }
    
    /**
     * Unregisters the production events listener
     * 
     * @param listener Listener to remove from the list
     */
    public void removeListener(ClientAgentListener listener) {
        listeners.remove(ClientAgentListener.class, listener);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void handleMessage(ACLMessage message) {
        JsonParser parser = new JsonParser();
        try {
            JsonObject msg = (JsonObject) parser.parse(message.getContent());
            Gson gson = new Gson();
            Action action = gson.fromJson(msg.get("action"), Action.class);
            if (action == Action.JOB_COMPLETED) {
                JobReport report = gson.fromJson(msg.get("object"), JobReport.class);
                jobCompleted(report);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Called upon receiving information about finished job.
     * 
     * @param report Detailed information about the job
     */
    private void jobCompleted(final JobReport report) {
        StringBuilder sb = new StringBuilder("Job ");
        String id = String.format("%x", report.getJobId()); 
        sb.append(id).append(": ");
        
        Level level;
        if (report.getStatus()) {
            sb.append("Success");
            level = Level.SUCCESS;
        } else {
            String description = report.getDescription();
            sb.append("Failure (").append(description).append(")");
            level = Level.ERROR;
        }
        String message = sb.toString();
        logger.log(message, level);
    }
    
    /**
     * Called upon receiving information about job acceptance/rejection.
     * 
     * @param report Detailed information about the job
     */
    private void requestProcessed(final JobRequestResponse response) {
        StringBuilder sb = new StringBuilder("Job ");
        String id = String.format("%x", response.getJobId());
        sb.append(id).append(": ");
        
        Level level;
        if (response.isAccepted()) {
            sb.append("Accepted");
            level = Level.SUCCESS;
        } else {
            String details = response.getDetails();
            sb.append("Rejected (").append(details).append(")");
            level = Level.ERROR;
        }
        String message = sb.toString();
        logger.log(message, level);
    }

    /**
     * Schedules a job for execution.
     * 
     * @param target Agent, to which direct the request 
     * @param job Details of the job to be scheduled
     */
    public void postJob(AID target, Job job) {
        addBehaviour(new JobExecutor(target, job));
    }
    
    /**
     * Sets up process of continuous querying DF about required services
     * using {@link ServiceLocator}
     */
    private void findServices() {
        locator = new ServiceLocator(this, Bank.SERVICE_NAME) {
            @Override
            protected int serviceFound(Set<AID> ids) {
                bank = ids.iterator().next();
                banks.addAll(ids);
                logger.success("Bank service found");
                return ServiceLocator.CONTINUE;
            }
            
            @Override
            protected int serviceUpdate(Set<AID> found, Set<AID> lost) {
                if (lost.contains(bank)) {
                    logger.error("Lost communication with primary bank");
                    bank = null;
                }
                if (! found.isEmpty()) {
                    logger.success("Bank service found");
                    if (bank == null) {
                        found.iterator().next();
                    }
                }
                banks.addAll(found);
                banks.removeAll(lost);
                return ServiceLocator.CONTINUE;
            }
            
            @Override protected int timeout() {
                logger.error("Unable to locate Bank (timeout)");
                return ServiceLocator.CONTINUE;
            }
        };
        addBehaviour(locator);
    }
    
    private void setupGUI() {
        Synchronous.invoke(new Runnable() {
            @Override
            public void run() {
                window = new ClientWindow(ClientAgent.this);
                window.setLocationRelativeTo(null);
                window.pack();
                window.setVisible(true);
                logger = window.getLogger().logSink();
            }
        });
    }
    
    @Override
    public void setup() {
        super.setup();
        setupGUI();
        // Search for 'environment' services
        findServices();
    }
    
    @Override
    public void takeDown() {
        removeBehaviour(locator);
    }

}
