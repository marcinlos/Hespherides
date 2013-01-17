package hesp.agents;

import hesp.gui.ManagerWindow;
import hesp.protocol.Protocols;
import jade.content.ContentManager;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author marcinlos
 *
 * Agent responsible for creation and initial configuration of the grid
 * simulation. 
 */
public class MetagridManager extends Agent {

    private static final Logger logger = Logger.getJADELogger("metagrid.manager");
    private static final Ontology ontology = JADEManagementOntology.getInstance();

    private ContainerID container;
    
    private ManagerWindow window;

    /**
     * Some boilerplate necessary to communicate with AMS
     */
    private void setupJade() throws Exception {
        String name = getContainerController().getContainerName();
        container = new ContainerID(name, null);

        getContentManager().registerOntology(ontology);
    }

    /**
     * Creates an agent of given class, with given name
     * 
     * @param clazz Class of the agent
     * @param name Name of the agent
     */
    private void createAgent(Class<?> clazz, String name) {
        try {
            // Fill description of an agent to be created
            CreateAgent ca = new CreateAgent();
            ca.setAgentName(name);
            ca.setClassName(clazz.getCanonicalName());
            ca.setContainer(container);
            
            Action act = new Action(getAMS(), ca);
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(getAMS());
            req.setOntology(ontology.getName());
            req.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            getContentManager().fillContent(req, act);
            
            // AMS sends a response, 
            addBehaviour(new AchieveREInitiator(this, req) {
                @Override
                protected void handleInform(ACLMessage inform) {
                    logger.severe("Success");
                }

                @Override
                protected void handleFailure(ACLMessage inform) {
                    logger.severe("Failure");
                }
            });
        } catch (CodecException | OntologyException e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printer = new PrintStream(out);
            e.printStackTrace(printer);
            logger.severe(out.toString());
        }
    }
    
    private void sendMessage(String agent, String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(agent, AID.ISLOCALNAME));
        msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        msg.setProtocol(Protocols.GRID);
        msg.setContent(message);
        send(msg);
    }
    
    /**
     * Registers this agent in yellow pages service.
     */
    private void registerService() {
        DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("controller");
        sd.setName("grid-controller");
        desc.addServices(sd);
        try {
            DFService.register(this, desc);
        } catch (FIPAException e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Sets up user interface
     */
    private void setupSwing() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                try {
                    for (LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                        if (info.getName().equals("Windows")) {
                            UIManager.setLookAndFeel(info.getClassName());
                        }
                    }
                } catch (ClassNotFoundException 
                        | InstantiationException
                        | IllegalAccessException 
                        | UnsupportedLookAndFeelException e) {
                    e.printStackTrace(System.err);
                }
                
                window = new ManagerWindow(MetagridManager.this);
                window.pack();
                window.setVisible(true);
            }
        });
    }
    
    /**
     * Creates initial set of agents.
     */
    private void setupAgents() {
        try {
            setupJade();
            //createAgent(Bank.class, "Bank");
            createAgent(ProductionAgent.class, "Res1");
            createAgent(ProductionAgent.class, "Res2");
            createAgent(SocialAgent.class, "Soc");
            createAgent(ClientAgent.class, "Client");

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    //TODO: Dirty checker
    public void createBank() {
        createAgent(Bank.class, "Bank");
    }
    
    public void destroyBank() {
        
    }

    @Override
    public void setup() {
        System.out.println("Creating metagrid environment...");
        try {
            ContentManager cm = getContentManager();
            cm.registerLanguage(new SLCodec());
            registerService();
            setupSwing();
            setupAgents();
            logger.info("Metagrid environment created");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Interprets and executes command from user interface.
     * 
     * @param text String passed from user interface
     */
    public void executeCommand(String text) {
        JsonParser parser = new JsonParser();
        try {
            JsonObject o = (JsonObject) parser.parse(text);
            interpretJson(o);
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
        }
    }
    
    
    private void interpretJson(JsonObject o) {
        String action = o.get("action").getAsString();
        if (action.equals("set_relation")) {
            String target = o.get("target").getAsString();
            o.remove("target");
            sendMessage(target, o.toString());
        } else if (action.equals("create")) {
            String className = o.get("class").getAsString();
            String name = o.get("name").getAsString();
            try {
                Class<?> clazz = Class.forName(className);
                createAgent(clazz, name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    
    @Override
    public void takeDown() {
        System.out.println("Shutting down metagrid...");

        System.out.println("Metagrid environment shut down");
    }

}
