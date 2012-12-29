package hesp.agents;

import hesp.gui.BankWindow;
import hesp.protocol.AccountCreation;
import hesp.protocol.AccountResponse;
import hesp.protocol.Action;
import hesp.protocol.Message;
import hesp.protocol.Transfer;
import hesp.protocol.TransferResponse;
import jade.core.AID;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class Bank extends HespAgent {

    private class Account {
        private String id;
        private AID owner;
        private int amount = 0;
        private List<AID> users;
        
        public Account(AID owner, String id, List<AID> users) {
            this.id = id;
            this.owner = owner;
            this.users = users;
        }
    }
    
    private Random rand = new Random();
    private Map<String, Account> accounts = new HashMap<>();
    private List<Account> accList = new ArrayList<>();
    
    private void registerAccount(Account account) {
        accounts.put(account.id, account);
        accList.add(account);
        
        model.fireTableDataChanged();
    }
    
    private BankWindow window;

    @Override
    protected void dispatchMessage(ACLMessage message) {
        Message<?> content = decode(message, Object.class);
        Action action = content.getAction();
        switch (action.category()) {
        case BANK:
            addBehaviour(new Transaction(message));
            break;
        }
    }
    
    private String genAccId() {
        return String.format("%x-%x", System.nanoTime(), rand.nextLong());
    }
    
    
    /**
     * Registers this agent in yellow pages service.
     */
    private void registerService() {
        DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("bank");
        sd.setName("grid-bank");
        desc.addServices(sd);
        try {
            DFService.register(this, desc);
        } catch (FIPAException e) {
            e.printStackTrace(System.err);
        }
    }

    private class TableModel extends AbstractTableModel {
        
        private final String[] cols = { "id", "owner", "amount" };
        
        public String getColumnName(int col) {
            return cols[col];
        }

        public int getRowCount() {
            return accList.size();
        }

        public int getColumnCount() {
            return cols.length;
        }

        public Object getValueAt(int row, int col) {
            Account acc = accList.get(row);
            switch (col) {
            case 0: return acc.id;
            case 1: return acc.owner.getLocalName();
            case 2: return acc.amount;
            default: return null;
            }
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void setValueAt(Object value, int row, int col) {
            throw new UnsupportedOperationException();
        }
    }
    
    private TableModel model = new TableModel();

    @Override
    public void setup() {
        try {
            super.setup();
            registerService();
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window = new BankWindow(getLocalName(), model);
                    window.setLocationRelativeTo(null);
                    window.pack();
                    window.setVisible(true);
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private class Transaction extends FSMBehaviour {

        private static final String ACCEPTANCE = "Acceptance";
        private static final String CREATION = "Account creation";
        private static final String TRANSFER = "Transfer";
        private static final String FAILURE = "Failure";
        private static final String SUCCESS = "Success";

        private static final int OK = 0;
        private static final int FAIL = 1;
        private static final int CREATION_T = 10;
        private static final int TRANSFER_T = 11;

        private static final int FIRST_MESSAGE = 10;
        private static final int CONTENT = 11;
        private static final int SENDER = 12;

        public Transaction(ACLMessage message) {
            registerTransition(ACCEPTANCE, CREATION, CREATION_T);
            registerTransition(ACCEPTANCE, TRANSFER, TRANSFER_T);
            registerTransition(CREATION, SUCCESS, OK);
            registerTransition(CREATION, FAILURE, FAIL);
            registerTransition(TRANSFER, SUCCESS, OK);
            registerTransition(TRANSFER, FAILURE, FAIL);

            getDataStore().put(FIRST_MESSAGE, message);
            
            registerFirstState(acceptance, ACCEPTANCE);
            registerState(creation, CREATION);
            registerState(transfer, TRANSFER);
            registerLastState(success, SUCCESS);
            registerLastState(failure, FAILURE);
        }

        private OneShotBehaviour acceptance = new OneShotBehaviour() {
            private int end;
            private DataStore data = Transaction.this.getDataStore();

            @Override
            public void action() {
                System.out.println("BANK: message got");
                ACLMessage message = (ACLMessage) data.get(FIRST_MESSAGE);
                Message<?> content = decode(message, Object.class);
                data.put(SENDER, message.getSender());
                switch (content.getAction()) {
                case CREATE_ACCOUNT:
                    end = CREATION_T;
                    data.put(CONTENT, decode(message, AccountCreation.class)
                            .getValue());
                    break;
                case TRANSFER:
                    end = TRANSFER_T;
                    data.put(CONTENT, decode(message, Transfer.class)
                            .getValue());
                    break;
                }
            }
            
            @Override
            public int onEnd() {
                return end;
            }
        };
        
        private boolean canCreate(AID client, AccountCreation creation) {
            return client.getLocalName().equals(creation.owner);
        }
        
        private boolean canTransfer(AID client, Transfer transfer, 
                Account acc) {
            if (acc.amount >= transfer.amount) {
                if (client.equals(acc.owner)) {
                    return true;
                } else if (acc.users.contains(client)) {
                    return true;
                } else {
                    return false;
                }
            }
            else { 
                return false;
            }
        }
        
        private OneShotBehaviour creation = new OneShotBehaviour() {
            private int end = OK;
            private DataStore data = Transaction.this.getDataStore();

            @Override
            public void action() {
                System.out.println("BANK: create account");
                ACLMessage message = (ACLMessage) data.get(FIRST_MESSAGE);
                ACLMessage reply = message.createReply();
                
                AccountCreation details = (AccountCreation) data.get(CONTENT);
                AID sender = (AID) data.get(SENDER);
                AccountResponse response;
                if (canCreate(sender, details)) {
                    String id = genAccId();
                    response = AccountResponse.success(id);
                    List<AID> ids = new ArrayList<>();
                    for (String name: details.users) {
                        ids.add(new AID(name, AID.ISLOCALNAME));
                    }
                    Account acc = new Account(sender, id, ids);
                    registerAccount(acc);
                } else {
                    response = AccountResponse.failure("Owner does not match");
                    end = FAIL;
                }
                sendMessage(reply, Action.CREATE_ACCOUNT_ACK, response);
            }
            
            @Override
            public int onEnd() {
                return end;
            }
        };
        
        
        private OneShotBehaviour transfer = new OneShotBehaviour() {
            private int end = OK;
            private DataStore data = Transaction.this.getDataStore();

            @Override
            public void action() {
                System.out.println("BANK: transfer");
                ACLMessage message = (ACLMessage) data.get(FIRST_MESSAGE);
                ACLMessage reply = message.createReply();
                Transfer transfer = (Transfer) data.get(CONTENT);
                AID sender = (AID) data.get(SENDER);
                
                Account source = accounts.get(transfer.sourceAccount);
                Account target = accounts.get(transfer.targetAccount);
                TransferResponse resp;

                if (source != null && target != null) {
                    if (canTransfer(sender, transfer, source)) {
                        resp = TransferResponse.success(transfer.id);
                        source.amount -= transfer.amount;
                        target.amount += transfer.amount;
                        end = FAIL;
                    } else {
                        resp = TransferResponse.failure(transfer.id, 
                                "Cannot perform operation");
                    }
                } else {
                    resp = TransferResponse.failure(transfer.id, "One of "+ 
                            "the accounts used in a transfer does not exist");
                    end = FAIL;
                }
                sendMessage(reply, Action.TRANSFER_ACK, resp);
            }
            
            @Override
            public int onEnd() {
                return end;
            }
        };
        
        private OneShotBehaviour failure = new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("BANK: Failure");
            }
        };
        
        private OneShotBehaviour success = new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("BANK: Success");
            }
        };
    }

}
