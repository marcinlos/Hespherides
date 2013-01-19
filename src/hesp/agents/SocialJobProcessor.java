package hesp.agents;

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
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Asynchronous job submission processor used by the social agent.
 * 
 * @author marcinlos
 */
class SocialJobProcessor extends FSMBehaviour {
    
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
    private static final int KEY_FAIL_REASON = 1000;
    private static final int KEY_FIRST_MSG = 1001;
    private static final int KEY_SENDER = 1002;
    private static final int KEY_JOB = 1003;
    private static final int KEY_CID = 1004;
    private static final int KEY_REPORT = 1005;
    
    private DataStore DS = getDataStore();
    private LogSink logger;
    private SocialAgent agent;
    
    public SocialJobProcessor(SocialAgent socialAgent, ACLMessage message) {

        super(socialAgent);
        this.agent = socialAgent;
        this.logger = agent.getLogger();
        
        registerTransition(ACCEPTANCE, POLICY_MAPPING, OK);
        registerTransition(POLICY_MAPPING, POLICY_ENFORCING, OK);
        registerTransition(POLICY_ENFORCING, REJECT, FAIL);
        registerTransition(POLICY_ENFORCING, DISCOVERY, OK);
        registerTransition(DISCOVERY, EXECUTION, OK);
        registerTransition(EXECUTION, EXEC_FAILURE, FAIL);
        registerTransition(EXECUTION, EXEC_SUCCESS, OK);
        registerTransition(EXEC_FAILURE, BILLING, OK);
        registerTransition(EXEC_SUCCESS, BILLING, OK);
        
        AID sender = message.getSender();
        DS.put(KEY_FIRST_MSG, message);
        DS.put(KEY_CID, message.getConversationId());
        DS.put(KEY_SENDER, sender);
        
        registerFirstState(new OneShotBehaviour(agent) {
            
            private int exitStatus = OK;
            
            @Override 
            public void action() {
                AID sender = (AID) DS.get(KEY_SENDER);
                ACLMessage first = (ACLMessage) DS.get(KEY_FIRST_MSG);
                String name = sender.getLocalName();
                logger.info("Incoming job request (from " + name + ")");
                Message<Job> content = Message.decode(first, Job.class);
                DS.put(KEY_JOB, content.getValue());
            }
            
            @Override
            public int onEnd() {
                return exitStatus;
            }
            
        }, ACCEPTANCE);
        
        registerState(new OneShotBehaviour(agent) {
            @Override 
            public void action() {
                System.out.println("Policy mapping");
            }
        }, POLICY_MAPPING);
        
        registerState(new OneShotBehaviour(agent) {
            @Override 
            public void action() {
                System.out.println("Policy enforcing");
            }
            @Override 
            public int onEnd() { 
                DS.put(KEY_FAIL_REASON, "I dont wanna");
                return Math.random() > 0.1 ? OK : FAIL; 
            }
        }, POLICY_ENFORCING);
        
        registerLastState(new OneShotBehaviour(agent) {
            @Override 
            public void action() {
                ACLMessage first = (ACLMessage) DS.get(KEY_FIRST_MSG);
                Job job = (Job) DS.get(KEY_JOB);
                ACLMessage reply = first.createReply();
                String reason = (String) DS.get(KEY_FAIL_REASON);
                JobRequestResponse response = new JobRequestResponse(
                        job.getId(), false, reason);
                agent.sendMessage(reply, Action.JOB_COMPLETED, response);
            }
        }, REJECT);
        
        registerState(new OneShotBehaviour(agent) {
            @Override 
            public void action() {
                AID aid = new AID("Res1", AID.ISLOCALNAME);
                AID sender = (AID) DS.get(KEY_SENDER);
                String cid = (String) DS.get(KEY_CID);
                
                final LinkSupervisionSlave slave = new LinkSupervisionSlave(
                        sender, (HespAgent) myAgent, cid);
                
                final LinkSupervisionMaster master = new LinkSupervisionMaster(
                        (HespAgent) myAgent, aid, cid) {
                    
                    @Override
                    protected int slaveTimeout() {
                        System.out.println("Production agent timed out");
                        Job job = (Job) DS.get(KEY_JOB);
                        JobReport report = new JobReport(job.getId(), false, 
                                "Production agent timed out");
                        DS.put(KEY_REPORT, report);
                        slave.stop(FAIL);
                        return FAIL;
                    }
                    
                    @Override
                    protected int finished() {
                        System.out.println("Production agent finished");
                        slave.stop(OK);
                        return OK;
                    }
                };
                
                ParallelBehaviour exec = new ParallelBehaviour() {
                    {
                        addSubBehaviour(slave);
                        addSubBehaviour(master);
                    }
                    
                    @Override
                    public int onEnd() {
                        super.onEnd();
                        return slave.getExitStatus();
                    }
                };
                registerState(exec, EXECUTION);
                
                // TODO: Make it real
                ACLMessage first = (ACLMessage) DS.get(KEY_FIRST_MSG);
                Job job = (Job) DS.get(KEY_JOB);
                ACLMessage reply = first.createReply();
                JobRequestResponse response = new JobRequestResponse(
                        job.getId(), true, "Request accepted");
                agent.sendMessage(reply, Action.JOB_SUBMITTED, response);
            }
        }, DISCOVERY);
        
        registerState(new OneShotBehaviour(agent) {
            @Override
            public void action() {
            }
        }, EXECUTION);
        
        registerState(new OneShotBehaviour(agent) {
            @Override 
            public void action() {
                logger.error("Execution failure");
                ACLMessage first = (ACLMessage) DS.get(KEY_FIRST_MSG);
                JobReport report = (JobReport) DS.get(KEY_REPORT);
                ACLMessage reply = first.createReply();
                agent.sendMessage(reply, Action.JOB_COMPLETED, report);
            }
        }, EXEC_FAILURE);
        
        registerState(new OneShotBehaviour(agent) {
            @Override 
            public void action() {
                ACLMessage first = (ACLMessage) DS.get(KEY_FIRST_MSG);
                JobReport report = (JobReport) DS.get(KEY_REPORT);
                ACLMessage reply = first.createReply();
                agent.sendMessage(reply, Action.JOB_COMPLETED, report);
            }
        }, EXEC_SUCCESS);
        
        registerLastState(new OneShotBehaviour(agent) {
            @Override 
            public void action() {
                System.out.println("It's pay time!");
            }
        }, BILLING);

    }
}