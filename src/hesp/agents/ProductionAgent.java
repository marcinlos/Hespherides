package hesp.agents;

import hesp.gui.ProductionWindow;
import hesp.gui.Synchronous;
import hesp.policy.Result;
import hesp.policy.TokenBasedUsage;
import hesp.policy.UnconditionalRefusal;
import hesp.policy.UsagePolicy;
import hesp.protocol.Action;
import hesp.protocol.Job;
import hesp.protocol.JobParameters;
import hesp.protocol.JobReport;
import hesp.protocol.JobRequestResponse;
import hesp.protocol.Message;
import hesp.util.LogSink;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.EventListenerList;

/**
 * @author marcinlos
 * 
 * Agent representing Grid Production Agent, directly supervising
 * underlying resources.
 */
public class ProductionAgent extends HespAgent implements JobProgressListener {

    private String name;
    private Computation resource;
    private AID creator;
    
    private Map<AID, AgentRelation> relations = new HashMap<>();

    private ProductionWindow window;
    private LogSink logger;
    
    /** List of production event listeners */
    private EventListenerList listeners = new EventListenerList();    
    
    
    private class PolicyManager {
        
        private Map<AID, UsagePolicy> policies = new HashMap<>();
        private static final String message = "You are not authorized to use me";
        private UsagePolicy defaultPolicy = new UnconditionalRefusal(message);
        
        public UsagePolicy getPolicy(AID agent, Job job) {
            UsagePolicy policy = policies.get(agent);
            if (policy == null) {
                policy = defaultPolicy;
            }
            return policy;
        }
    }
    
    private PolicyManager policyManager = new PolicyManager();
    
    
    public Computation getComputation() {
        return this.resource;
    }
    
    
    protected boolean authorizeControl(AID sender) {
        if (sender.equals(creator)) {
            return true;
        } else {
            AgentRelation relation = relations.get(sender);
            if (relation == AgentRelation.OWNAGE) {
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    protected boolean dispatchMessage(ACLMessage message) {
        Message<?> content = Message.decode(message, Object.class);
        Action action = content.getAction();
        switch (action.category()) {
        case CONTROL: 
            addBehaviour(new ControlProcessor(message));
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
        
        private Job job;
        private AID sender;
        private JobReport report;
        
        private static final String ACCEPTANCE = "Acceptance";
        private static final String POLICY_MAPPING = "Policy mapping";
        private static final String POLICY_ENFORCING = "Policy enforcing";
        private static final String REJECT = "Reject";
        private static final String SUBMIT = "Submit";
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
        private static final int KEY_POLICY = 4321;
        
        private DataStore DS = getDataStore();

        public JobSubmissionProcessor(final ACLMessage firstMessage) {

            registerTransition(ACCEPTANCE, POLICY_MAPPING, OK);
            registerTransition(ACCEPTANCE, REJECT, FAIL);
            registerTransition(POLICY_MAPPING, POLICY_ENFORCING, OK);
            registerTransition(POLICY_ENFORCING, REJECT, FAIL);
            registerTransition(POLICY_ENFORCING, SUBMIT, OK);
            registerTransition(SUBMIT, EXECUTION, OK);
            registerTransition(EXECUTION, EXEC_FAILURE, FAIL);
            registerTransition(EXECUTION, EXEC_SUCCESS, OK);
            registerTransition(EXEC_FAILURE, BILLING, OK);
            registerTransition(EXEC_SUCCESS, BILLING, OK);
            
            sender = firstMessage.getSender();
            
            //-------------------------------------------------------
            // Indentation smaller for clarity
            //-------------------------------------------------------

    /*
     * Initial state, we choose wether or not to process the request based
     * on amount of queued jobs.
     */
    registerFirstState(new OneShotBehaviour() {
        
        private int end = OK;
        
        @Override public void action() {
            String name = sender.getLocalName();
            logger.info("Incoming job request (from " + name + ")");
            Message<Job> content = Message.decode(firstMessage, Job.class);
            job = content.getValue();
            int queued = resource.queuedJobs();
            float work = (float) queued / resource.getProcessors();
            if (work > 1.2) {
                end = FAIL;
                DS.put(KEY_FAIL_REASON, "Too many queued jobs");
            }
        }
        
        @Override
        public int onEnd() {
            return end;
        }
        
    }, ACCEPTANCE);
    
    /*
     * We use the policy manager to map the request to the policy
     */
    registerState(new OneShotBehaviour() {
        @Override
        public void action() {
            //System.out.println("Policy mapping");
            UsagePolicy policy = policyManager.getPolicy(sender, job);
            DS.put(KEY_POLICY, policy);
        }
    }, POLICY_MAPPING);
    
    registerState(new OneShotBehaviour() {
        
        private int end = OK;
        
        @Override public void action() {
            //System.out.println("Policy enforcing");
            UsagePolicy policy = (UsagePolicy) DS.get(KEY_POLICY);
            Result result = policy.use(job);
            if (! result.result) {
                DS.put(KEY_FAIL_REASON, result.message);
                end = FAIL;
            }
        }

        @Override 
        public int onEnd() {
            return end; 
        }
        
    }, POLICY_ENFORCING);
    
    // Job request was rejected due to policy & modality constraints
    registerLastState(new OneShotBehaviour() {
        @Override public void action() {
            //System.out.println("Rejection");
            ACLMessage reply = firstMessage.createReply();
            String reason = (String) DS.get(KEY_FAIL_REASON);
            JobRequestResponse resp = 
                    new JobRequestResponse(job.getId(), false, reason);
            sendMessage(reply, Action.JOB_COMPLETED, resp);
        }
    }, REJECT);
    
    final class ExecSupervisor extends ParallelBehaviour {
        private int end = OK;
        LinkSupervisionSlave slave;
        Behaviour waiter;

        private ExecSupervisor(String cid) {
            slave = new LinkSupervisionSlave(sender, ProductionAgent.this,
                    cid) {
                @Override
                protected int masterTimeout() {
                    logger.error("Master timeout");
                    return FAIL;
                }
            };

            waiter = new Behaviour() {
                private boolean run = true;

                @Override
                public void action() {
                    String cid = "job" + job.getId();
                    MessageTemplate template = MessageTemplate
                            .MatchConversationId(cid);
                    ACLMessage reply = receive(template);
                    if (reply != null) {
                        slave.stop(end);
                        Message<JobReport> rep = Message.decode(reply,
                                JobReport.class);
                        report = rep.getValue();
                        run = false;
                        end = report.getStatus() ? OK : FAIL;
                    } else {
                        block();
                    }
                }

                @Override
                public boolean done() {
                    return !run;
                }
            };

            addSubBehaviour(slave);
            addSubBehaviour(waiter);
        }

        @Override
        public int onEnd() {
            return end;
        }
    }
    
    // All the constraints are met, request is about to be scheduled for
    // execution
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("Submitting");
            ACLMessage reply = firstMessage.createReply();
            JobRequestResponse resp = new JobRequestResponse(
                    job.getId(), true, "Request accepted");
            sendMessage(reply, Action.JOB_SUBMITTED, resp);
            submitJob(job);
            
            final String cid = firstMessage.getConversationId(); 
            
            Behaviour b = new ExecSupervisor(cid);
            registerState(b, EXECUTION);
        }
    }, SUBMIT);
    
    /*
    // Job is being executed, wait for results
    registerState(new Behaviour() {
        private boolean run = true;
        private int end = OK;
        
        @Override
        public void action() {
            String cid = "job" + job.getId();
            MessageTemplate template = MessageTemplate.MatchConversationId(cid);
            ACLMessage reply = receive(template);
            if (reply != null) {
                Message<JobReport> rep = decode(reply, JobReport.class);
                report = rep.getValue();
                run = false;
                end = report.getStatus() ? OK : FAIL;
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
    }, EXECUTION);
    */
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
    
    private class ControlProcessor extends SequentialBehaviour {
        
//        private ACLMessage message;
//        private String cid;
        
        public ControlProcessor(ACLMessage message) {
            //this.message = message;
            //cid = genCID();
        }
        
    }
    
    /**
     * Registers new production events listener.
     * 
     * @param listener Listener to add to list
     */
    public void addListener(ProductionListener listener) {
        listeners.add(ProductionListener.class, listener);
    }
    
    /**
     * Unregisters the production events listener
     * 
     * @param listener Listener to remove from the list
     */
    public void removeListener(ProductionListener listener) {
        listeners.remove(ProductionListener.class, listener);
    }
    

    /*
    private void handleControlMessage(ACLMessage full, JsonObject message) {
        boolean auth = authorizeControl(full.getSender());
        Gson gson = new Gson();
        Action action = gson.fromJson(message.get("action"), Action.class);
        if (auth) {
            if (action == Action.SET_OWNER) {
                JsonObject value = message.get("object").getAsJsonObject();
                JsonElement jrel = value.get("relation");
                AgentRelation rel = gson.fromJson(jrel, AgentRelation.class);
                String agent = value.get("agent").getAsString();
                AID agentAID = new AID(agent, AID.ISLOCALNAME);
                relations.put(agentAID, rel);
                System.out.println("'" + name + "' is now in in relation " + 
                        rel + " with " + agent);
            }
        }
    }*/
    
    private ProductionListener[] listenerList() {
        return listeners.getListeners(ProductionListener.class);
    }
    
    private void submitJob(Job job) {
        resource.queueJob(job);
        long id = job.getId();
        JobParameters params = job.getParameters();
        JobProgress status = new JobProgress(id, params.getCputime());
        for (ProductionListener listener: listenerList()) {
            listener.jobQueued(status);
        }
    }
    
    
    /**
     * Queries yellow pages service for grid controller agent.
     */
    private void findMaster() {
        DFAgentDescription pattern = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName(MetagridManager.SERVICE_NAME);
        pattern.addServices(sd);
        DFAgentDescription[] creators = null;
        try {
            creators = DFService.search(this, pattern);
            creator = creators[0].getName();
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setup() {
        super.setup();

        name = getLocalName();
        findMaster();
        
        resource = new Computation(this, 7, 50, this);
        addBehaviour(resource);
        
        setupGUI();
        
        policyManager.policies.put(new AID("Client", AID.ISLOCALNAME), 
                new TokenBasedUsage(2000000000));
        logger.success("PGA '" + name + "' created");
    }


    private void setupGUI() {
        Synchronous.invoke(new Runnable() {
            @Override
            public void run() {
                window = new ProductionWindow(ProductionAgent.this);
                addListener(window);
                window.pack();
                window.setVisible(true);
                logger = window.getLogger().logSink();
            }
        });
    }

    @Override
    public void takeDown() {
        logger.info("PGA '" + name + "' shutting down");
    }

    /**
     * Invoked on computation tick for each job in progress.
     */
    @Override
    public void update(JobProgress job) {
        for (ProductionListener listener: listenerList()) {
            listener.jobUpdate(job);
        }
    }

    /**
     * Invoked upon completion of a job.
     */
    @Override
    public void completed(JobProgress job, String details) {
        final boolean success = job.hasSucceeded();
        JobReport report = new JobReport(job.getId(), success, details);
        ACLMessage message = emptyMessage(ACLMessage.INFORM);
        message.addReceiver(getAID());
        message.setConversationId("job" + job.getId());
        sendMessage(message, Action.JOB_COMPLETED, report);
        
        for (ProductionListener listener: listenerList()) {
            listener.jobFinished(job);
        }
    }


    @Override
    public void started(JobProgress job) {
        for (ProductionListener listener: listenerList()) {
            listener.jobStarted(job);
        }
    }

}
