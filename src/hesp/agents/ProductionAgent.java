package hesp.agents;

import hesp.agents.Computation.JobStatus;
import hesp.gui.ProductionWindow;
import hesp.protocol.Action;
import hesp.protocol.Job;
import hesp.protocol.JobReport;
import hesp.protocol.JobRequestResponse;
import hesp.protocol.Message;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

/**
 * @author marcinlos
 * 
 * Agent representing Grid Production Agent, directly supervising
 * underlying resources.
 */
public class ProductionAgent extends HespAgent implements Computation.Listener {

    private String name;
    private Computation resource;
    private AID creator;
    
    private Map<AID, AgentRelation> relations = new HashMap<>();
    
    
    private ProductionWindow window;
    private CountDownLatch sync = new CountDownLatch(1);
    
    private class PolicyManager {
        
        private Map<AID, UsagePolicy> policies = new HashMap<>();
        
        public boolean canUse(AID agent, Job job) {
            UsagePolicy policy = policies.get(agent);
            if (policy != null) {
                return policy.canUse(job);
            } else {
                return false;
            }
        }
        
        public boolean use(AID agent, Job job) {
            UsagePolicy policy = policies.get(agent);
            if (policy != null) {
                return policy.use(job);
            } else {
                return false;
            }
        }
        
    }
    
    private PolicyManager policy = new PolicyManager();
    
    
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
    protected void dispatchMessage(ACLMessage message) {
        Message<?> content = decode(message, Object.class);
        Action action = content.getAction();
        switch (action.category()) {
        case CONTROL: 
            addBehaviour(new ControlProcessor(message));
            break;
        case JOB:
            addBehaviour(new JobSubmissionProcessor(message));
            break;
        }
    }
    
    /**
     * Asynchronous job submission processor
     */
    private class JobSubmissionProcessor extends FSMBehaviour {
        //private MessageTemplate template;
        //private String cid;
        private Job job;
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
        
        public JobSubmissionProcessor(final ACLMessage firstMessage) {
            //cid = genCID();
            /*this.template = MessageTemplate.and(
                    MessageTemplate.MatchProtocol(Protocols.GRID),
                    MessageTemplate.MatchConversationId(cid));*/

            registerTransition(ACCEPTANCE, POLICY_MAPPING, OK);
            registerTransition(POLICY_MAPPING, POLICY_ENFORCING, OK);
            registerTransition(POLICY_ENFORCING, REJECT, FAIL);
            registerTransition(POLICY_ENFORCING, SUBMIT, OK);
            registerTransition(SUBMIT, EXECUTION, OK);
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
            Message<Job> content = decode(firstMessage, Job.class);
            job = content.getValue();
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
            return Math.random() > 0.5 ? OK : FAIL; 
        }
    }, POLICY_ENFORCING);
    
    // Job request was rejected due to policy & modality constraints
    registerLastState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("Rejection");
            ACLMessage reply = firstMessage.createReply();
            JobRequestResponse resp = new JobRequestResponse(
                    job.getId(), false, "Request arbitrarily rejected");
            sendMessage(reply, Action.JOB_COMPLETED, resp);
        }
    }, REJECT);
    
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
        }
    }, SUBMIT);
    
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
    
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("Execution failure");
            ACLMessage reply = firstMessage.createReply();
            sendMessage(reply, Action.JOB_COMPLETED, report);
        }
    }, EXEC_FAILURE);
    
    registerState(new OneShotBehaviour() {
        @Override public void action() {
            System.out.println("Execution success");
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
        
        private ACLMessage message;
        private String cid;
        
        public ControlProcessor(ACLMessage message) {
            this.message = message;
            cid = genCID();
        }
        
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
    
    
    private void submitJob(Job job) {
        resource.queueJob(job);
        window.addJob(new JobStatus(job.getId(), 2 * job.getCputime()));
    }
    
    
    /**
     * Queries yellow pages service for grid controller agent.
     */
    private void findMaster() {
        DFAgentDescription pattern = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("grid-controller");
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
        
        resource = new Computation(this, 7, this);
        addBehaviour(resource);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window = new ProductionWindow(name);
                window.pack();
                window.setVisible(true);
                sync.countDown();
            }
        });
        try {
            sync.await();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
        
        policy.policies.put(new AID("Client", AID.ISLOCALNAME), new TokenBasedUsage(4));
        
        System.out.println("PGA '" + name + "' created");
    }

    @Override
    public void takeDown() {
        System.out.println("PGA '" + name + "' shutting down");
    }

    /**
     * Invoked on computation tick for each job in progress.
     */
    @Override
    public void update(final JobStatus job) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                window.update(job);
            }
        });
        
    }

    /**
     * Invoked upon completion of a job.
     */
    @Override
    public void completed(final JobStatus job, String details) {
        final boolean success = job.success();
        JobReport report = new JobReport(job.getId(), success, details);
        ACLMessage message = emptyMessage(ACLMessage.INFORM);
        message.addReceiver(getAID());
        message.setConversationId("job" + job.getId());
        sendMessage(message, Action.JOB_COMPLETED, report);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                window.finished(job, success);
            }
        });
    }

}
