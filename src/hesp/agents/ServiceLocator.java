package hesp.agents;

import jade.core.AID;
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

import java.util.HashSet;
import java.util.Set;

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
    
    /** Name of service this instance is supposed to keep track of */
    private String serviceName;
    
    /** Currently known service providers */
    private Set<AID> providers = new HashSet<>();
    
    /** Key with witch results are associated in data store */
    private static final int RESULT_KEY = 132435;
    
    /** Time between initial and second query */
    private final int initialDelay = 200;
    /**
     * Maximum numer of ignored unsuccessful queries - i.e. number of attempts
     * in exponential backoff algorithm
     */
    private final int maxAttempts = 5;
    
    /**
     * Time between consecutive updates, if this mode has been chosen after
     * the first successful query
     */
    private int updateSlot = 5000;

    /**
     * Creates a new service locator bound to {@code agent} and keeping track
     * of available {@code serviceName} providers.
     * 
     * @param agent Agent on whose behalf queries shall be performed
     * @param serviceName Name of service to query 
     */
    public ServiceLocator(Agent agent, String serviceName) {
        this.agent = agent;
        this.serviceName = serviceName;
        // initialize FSM
        setupFSM();
    }

    /**
     * Registers states and transitions, FSM creation
     */
    private void setupFSM() {
        registerTransition(INITIAL, SEARCHING, T_OK, new String[]{SEARCHING});
        registerTransition(INITIAL, HAS_FOUND, T_FOUND, new String[]{HAS_FOUND});
        registerTransition(SEARCHING, TIMED_OUT, T_FAIL, new String[]{TIMED_OUT});
        registerTransition(SEARCHING, HAS_FOUND, T_FOUND, new String[]{HAS_FOUND});
        registerTransition(HAS_FOUND, HALT, STOP);
        registerTransition(HAS_FOUND, UPDATING, CONTINUE, new String[]{UPDATING});
        registerTransition(UPDATING, INITIAL, RESTART, new String[]{INITIAL});
        registerTransition(UPDATING, UPDATING, IGNORE);
        registerTransition(UPDATING, HALT, STOP);
        registerTransition(TIMED_OUT, HALT, STOP);
        registerTransition(TIMED_OUT, UPDATING, CONTINUE);
        
        registerFirstState(initial, INITIAL);
        registerState(searching, SEARCHING);
        registerState(hasFound, HAS_FOUND);
        registerState(timedOut, TIMED_OUT);
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
     * @return set of providing agents' identifiers, or {@code null} if an
     * error occurred
     */
    private Set<AID> tryLocate() {
        DFAgentDescription pattern = new DFAgentDescription();
        ServiceDescription serviceDesc = new ServiceDescription();
        serviceDesc.setName(serviceName);
        pattern.addServices(serviceDesc);
        DFAgentDescription[] results = null;

        try {
            results = DFService.search(agent, pattern);
            Set<AID> resultSet = new HashSet<>();
            for (DFAgentDescription desc : results) {
                resultSet.add(desc.getName());
            }
            return resultSet;
        } catch (FIPAException e) {
            // Exception is treated as a failed query, though it may be 
            // a symptom of more severe condition. Let the user handle it.
            fail(e);
            return null;
        }
    }
    
    /**
     * Saves found providers, and invokes {@link #serviceFound(Set)} callback
     * method.
     * 
     * @param found Set of found providers
     * @return action code, as returned by {@link #serviceFound(Set)}
     */
    private int notifyServiceFound(Set<AID> found) {
        providers = found;
        return serviceFound(found);
    }
    
    /**
     * Updates internal state and invokes {@link #serviceUpdate(Set)} callback
     * method.
     * 
     * @param update Set of just found providers (all returned by the DF query)
     * @return action code, as retudned by {@link #serviceUpdate(Set)}
     */
    private int notifyUpdate(Set<AID> update) {
        Set<AID> found = new HashSet<AID>(update);
        found.removeAll(providers);
        Set<AID> lost = new HashSet<AID>(providers);
        lost.removeAll(update);
        providers = update;
        return serviceUpdate(found, lost);
    }
    
    private OneShotBehaviour initial = new OneShotBehaviour() {
        
        private int end = T_OK;
        
        @Override
        public void action() {
            // Try to find the service
            Set<AID> found = tryLocate();
            if (! found.isEmpty()) {
                getDS().put(RESULT_KEY, found);
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
        private int end = T_FAIL;
        private State state = new State();
        
        @Override
        protected void onTick() {
            Set<AID> found = tryLocate();
            // Success - we can move on
            if (! found.isEmpty()) {
                getDS().put(RESULT_KEY, found);
                end = T_FOUND;
                stop();
            } else {
                if (state.attempts < maxAttempts) {
                    ++ state.attempts;
                    state.delay *= 2;
                    reset(state.delay);
                } else {
                    stop();
                }
            }
        }
        
        @Override
        public int onEnd() {
            // restore the initial state
            state = new State();
            return end;
        }
    };
    
    private Behaviour hasFound = new OneShotBehaviour() {
        
        private int end;
        
        @Override
        public void action() {
            @SuppressWarnings("unchecked")
            Set<AID> ids = (Set<AID>) getDS().get(RESULT_KEY);
            int next = notifyServiceFound(ids);
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
            Set<AID> res = tryLocate();
            if (! res.isEmpty()) {
                int action = notifyUpdate(res);
                if (action == STOP) {
                    end = STOP;
                    stop();
                } else if (action == CONTINUE) {
                    end = CONTINUE;
                } else if (action == RESTART) { 
                    end = RESTART;
                    stop();
                } else {
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
            } else if (action == CONTINUE) {
                end = CONTINUE; 
            } else if (action == STOP) {
                end = STOP;
            } else {
                fail(new IllegalStateException("Invalid action returned"));
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
            // Empty for now
        }
    };

    /**
     * Invoked when the service provider has been found for the first time,
     * that is, every time .
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
    abstract protected int serviceFound(Set<AID> ids);
    
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
     * @param found Set of agents just found during this update, i.e. agents
     * that have not been present in the previous update
     * 
     * @param lost Set of agents lost between last updates, i.e. agents that
     * have been present in the previous update, but are not in the current.
     * 
     * @return action code
     */
    protected int serviceUpdate(Set<AID> found, Set<AID> lost) {
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