package hesp.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Abstract class to keep track of services like banking system. Searches
 * for the service using configurable exponential backoff algorithm,
 * and periodically updates the results. The user is expected to extend
 * this class, overriding some of the following methods:
 * <ul>
 * <li>{@code serviceFound}
 * <li>{@code serviceLost}
 * <li>{@code timeout}
 * <li>{@code fail}
 * </ul>
 * 
 * @author marcinlos
 */
abstract class ServiceLocator extends FSMBehaviour {
    
    private static final String INITIAL = "Initial";
    private static final String SEARCHING = "Searching";
    private static final String HAS_FOUND = "Has found";
    private static final String TIMED_OUT = "Timed out";
    private static final String AFTER_TIMEOUT = "After timeout";
    private static final String UPDATING = "Updating";
    private static final String HALT = "Halt";
    
    private static final int T_OK = 0;
    private static final int T_FAIL = 1;
    private static final int T_FOUND = 2;
    
    /**
     * Constant to be returned from callbacks. When returned, it means the user
     * requests the {@code ServiceLocator}'s algorithm to start completely
     * anew.
     */
    public static final int RESTART = 10;
    
    /**
     * Constant to be returned from callbacks. When returned, it means the user
     * requests the failure of communication which caused the callback to be
     * invoked is to be ignored, i.e. {@code ServiceLoader} shall continue
     * as if the failure has not occurred.
     */
    public static final int IGNORE = 11;
    
    /**
     * Constant to be returned from callbacks. When returned, it means the user
     * requests the querying process to finish.
     */
    public static final int STOP = 12;
    
    /**
     * Constant to be returned from callbacks. When returned, it means the user
     * requests the algorithm to continue working, as opposed to halting,
     * after the requested service has been found, or timeout has occurred.
     */
    public static final int CONTINUE = 13;

    private Agent agent;
    private String name;
    
    private static final int RESULT_KEY = 132435;
    
    private final int initialDelay = 50;
    private final int maxAttempts = 5;
    private int updateSlot = 5000;

    
    public ServiceLocator(Agent agent, String name) {
        this.agent = agent;
        this.name = name;
        
        registerTransition(INITIAL, SEARCHING, T_OK);
        registerTransition(INITIAL, HAS_FOUND, T_FOUND);
        registerTransition(SEARCHING, TIMED_OUT, T_FAIL);
        registerTransition(SEARCHING, HAS_FOUND, T_FOUND);
        registerTransition(HAS_FOUND, HALT, STOP);
        registerTransition(HAS_FOUND, UPDATING, CONTINUE);
        registerTransition(UPDATING, INITIAL, RESTART);
        registerTransition(UPDATING, UPDATING, IGNORE);
        registerTransition(UPDATING, HALT, STOP);
        registerTransition(TIMED_OUT, AFTER_TIMEOUT, CONTINUE);
        registerTransition(TIMED_OUT, HALT, STOP);
        registerTransition(AFTER_TIMEOUT, HAS_FOUND, T_FOUND);
        
        registerFirstState(initial, INITIAL);
        registerState(searching, SEARCHING);
        registerState(hasFound, HAS_FOUND);
        registerState(timedOut, TIMED_OUT);
        registerState(afterTimeout, AFTER_TIMEOUT);
        registerState(updating, UPDATING);
        registerLastState(halt, HALT);
    }
    
    /** Helper for behaviour implementations */
    private DataStore getDS() {
        return getDataStore();
    }
    
    /**
     * Sends request to DF agent to get information about agents with
     * matching service name.
     * 
     * @return array of matching agents' descriptions, or {@code null}
     * if no agents are found, or an exception was thrown by DF search method.
     */
    private DFAgentDescription[] tryLocate() {
        DFAgentDescription pattern = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName(name);
        pattern.addServices(sd);
        DFAgentDescription[] results = null;
        try {
            results = DFService.search(agent, pattern);
            if (results.length > 0) {
                return results;
            } else {
                return null;
            }
        } catch (FIPAException e) {
            fail(e);
            return null;
        }
    }
    
    private OneShotBehaviour initial = new OneShotBehaviour() {
        
        private int end = T_OK;
        
        @Override
        public void action() {
            // Try to find the service
            DFAgentDescription[] res = tryLocate();
            if (res != null) {
                getDS().put(RESULT_KEY, res);
                end = T_FOUND;
            } else {
                // Initiate exponential backoff
                end = T_OK;
            }
        }
        
        @Override
        public int onEnd() {
            return end;
        }
    };
    
    /**
     * Periodically querying DF about the service
     */
    private Behaviour searching = new TickerBehaviour(agent, initialDelay) {
                
        final class State {
            int attempts = 0;
            int delay = initialDelay;
        }   
        int end = T_FAIL;
        private State state = new State();
        
        @Override
        protected void onTick() {
            System.out.println("Attempt " + state.attempts);
            DFAgentDescription[] res = tryLocate();
            // Success - we can move on
            if (res != null) {
                getDS().put(RESULT_KEY, res);
                end = T_FOUND;
                stop();
            } else {
                if (state.attempts < maxAttempts) {
                    ++ state.attempts;
                    state.delay *= 2;
                    reset(state.delay);
                } else {
                    // Restore initial state
                    state = new State();
                    stop();
                }
            }
        }
        
        @Override
        public int onEnd() {
            return end;
        }
    };
    
    private Behaviour hasFound = new OneShotBehaviour() {
        
        private int end;
        
        @Override
        public void action() {
            System.out.println("Has found it!");
            DFAgentDescription[] results = 
                    (DFAgentDescription[]) getDS().get(RESULT_KEY);
            int next = serviceFound(results);
            if (next == STOP) {
                end = STOP;
            } else if (next == CONTINUE) {
                end = CONTINUE;
            } else {
                fail(new IllegalStateException("Invalid return " + 
                        "value: " + next));
            }
        }
        
        @Override
        public int onEnd() {
            return end;
        }
    };
    
    /**
     * Having found a service, {@code ServiceLocator} may continue to 
     * periodically query DF about this service 
     */
    private Behaviour updating = new TickerBehaviour(agent, updateSlot) {
        
        private int end = T_OK;
        
        @Override
        protected void onTick() {
            System.out.println("Updating...");
            DFAgentDescription[] res = tryLocate();
            if (res != null) {
                int action = serviceUpdate(res);
                if (action == STOP) {
                    end = STOP;
                    stop();
                } else if (action != CONTINUE) {
                    fail(new IllegalStateException("Invalid action returned"));
                }
            } else {
                int action = serviceLost();
                if (action == STOP) {
                    end = STOP;
                    stop();
                } else if (action != CONTINUE) {
                    fail(new IllegalStateException("Invalid action returned"));
                }
            }
        }
        
        @Override
        public int onEnd() {
            return end;
        }
    };
    
    /**
     * FSM moves to this state after having exceeded {@code maxAttempts}
     * attempts to locate a service.
     */
    private Behaviour timedOut = new OneShotBehaviour() {
        
        private int end = T_OK;
        
        @Override
        public void action() {
            int action = timeout();
            if (action == RESTART) {
                end = RESTART;
                ServiceLocator.this.restart();
            } else if (action == CONTINUE) {
                end = CONTINUE; 
            } else if (action == STOP) {
                end = STOP;
            }
        }
        
        @Override
        public int onEnd() {
            return end;
        }
    };
    
    private Behaviour afterTimeout = new TickerBehaviour(agent, updateSlot) {

        private int end = CONTINUE;

        @Override
        protected void onTick() {
            System.out.println("Still trying...");
            DFAgentDescription[] res = tryLocate();
            if (res != null) {
                end = T_FOUND;
                stop();
            }
        }

        @Override
        public int onEnd() {
            return end;
        }
    };
    
    /**
     * Final state of the algorithm
     */
    private OneShotBehaviour halt = new OneShotBehaviour() {
        @Override
        public void action() {
            System.out.println("ServiceLocator: HALT");
        }
    };

    /**
     * Invoked when the service has been found.
     * <p>
     * Return value determines the action to undertake next. Following values
     * are currently supported for this method:
     * <ul>
     * <li> {@link #CONTINUE} - switch to Updating mode, and
     * continue to periodically query DF about the service providers.
     * 
     * <li> {@link #STOP} - finish the algorithm
     * </ul>
     * 
     * @param ids Information about discovered service providers
     * @return action code
     */
    abstract protected int serviceFound(DFAgentDescription[] ids);
    
    /**
     * Invoked after each update in Updating mode.
     * <p>
     * Return value determines the action to undertake next. Supported 
     * constants and their meaning are analogous to {@link #serviceFound}:
     * <ul>
     * <li> {@link #CONTINUE} - continue in Updating mode
     * 
     * <li> {@link #STOP} - finish the algorithm
     * </ul>
     * Default implementation returns {@link #CONTINUE}.
     * 
     * @param ids Array of agent description structures, containing information
     * about services matching the passed description.
     * 
     * @return action code
     */
    protected int serviceUpdate(DFAgentDescription[] ids) {
        return CONTINUE;
    }
        
    /**
     * Invoked when an unexpected exceptino was thrown during the process of
     * service discovery.
     * <p>
     * Default implementation swallows the exception.
     * 
     * @param e Exception that has been thrown unexpectedly
     */
    protected void fail(Exception e) {
    
    }
    
    /**
     * Invoked when the service has ceased to be visible during updates. 
     * <p>
     * The return value determines an action the {@code ServiceLocator} shall
     * undertake next. Following return values are currently supported:
     * <ul>
     * <li>{@link #CONTINUE} - switch to communication failure recovery state
     * 
     * <li>{@link #IGNORE} - ignore the failure, and continue to operate
     * in Updating state
     * 
     * <li>{@link #STOP} - finish the algorithm
     * </ul>
     * Default implementation returns {@code #IGNORE}.
     * 
     * @return action code
     */
    protected int serviceLost() {
        return IGNORE;
    }
    
    /**
     * Invoked when exponential backoff algorithm has reached a maximal number
     * of attempts, and service still has not been found.
     * <p>
     * Return value determines the action to undertake next. Following return
     * values are currently supported:
     * <ul>
     * <li>{@link #RESTART} - stop the whole algorithm and start from the 
     * initial state
     * 
     * <li>{@link #CONTINUE} - move to communication failure recovery state
     * 
     * <li>{@link #STOP} - finish the algorithm
     * </ul>
     * Default implementation returns {@code #STOP}.
     * 
     * @return action code
     */
    protected int timeout() {
        return STOP;
    }

}