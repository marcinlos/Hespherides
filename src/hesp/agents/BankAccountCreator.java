package hesp.agents;

import com.google.gson.JsonSyntaxException;

import hesp.protocol.AccountCreation;
import hesp.protocol.AccountResponse;
import hesp.protocol.Action;
import hesp.protocol.Action.Category;
import hesp.protocol.Message;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Account creation protocol implementation.
 * 
 * @author marcinlos
 */
public class BankAccountCreator extends SequentialBehaviour {
    
    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
    
    private int exitStatus;
    
    public BankAccountCreator(final HespAgent agent, 
            final AccountCreation account, final AID bank) {
        super(agent);

        // Sending the message
        addSubBehaviour(new OneShotBehaviour(agent) {
            @Override
            public void action() {

                ACLMessage message = agent.emptyInitMessage(ACLMessage.REQUEST);
                message.addReceiver(bank);
                agent.sendMessage(message, Action.CREATE_ACCOUNT, account);
                String cid = message.getConversationId();
                
                MessageTemplate template = MessageTemplate.and(
                        MessageTemplate.MatchConversationId(cid),
                        Action.MatchCategory(Category.BANK));
                System.out.println("CID = " + cid);
                
                Behaviour b = new TimedReceiver(myAgent, 3000, template) {
                    @Override
                    protected boolean handleMessage(ACLMessage message) {
                        try {
                            Message<AccountResponse> msg = Message.decode(
                                    message, AccountResponse.class);
                            AccountResponse resp = msg.getValue();
                            if (resp.succeeded) {
                                setExitStatus(SUCCESS);
                                handleSuccess(resp);
                            } else {
                                setExitStatus(FAILURE);
                                handleFailure();
                            }
                        } catch (JsonSyntaxException e) {
                            setExitStatus(FAILURE);
                            handleFailure();
                        }
                        return true;
                    }
                    
                    @Override
                    public void handleTimeout() {
                        setExitStatus(FAILURE);
                        BankAccountCreator.this.handleTimeout();
                    }
                };
                addSubBehaviour(b);
            }
        });
    }
    
    
    public int getExitStatus() {
        return exitStatus;
    }
    
    protected void setExitStatus(int code) {
        this.exitStatus = code;
    }
    
    @Override
    public int onEnd() {
        super.onEnd();
        return exitStatus;
    }

    protected void handleSuccess(AccountResponse response) {
        // To be overriden
    }
    
    protected void handleFailure() {
        // To be overriden
    }
    
    protected void handleTimeout() {
        // To be overriden
    }
}
