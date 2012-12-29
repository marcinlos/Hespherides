package hesp.protocol;

import hesp.agents.AgentRelation;

import java.io.Serializable;

public class RelationMessage implements Serializable {
    
    private String agent;
    private AgentRelation relation;
    
    public RelationMessage(String agent, AgentRelation relation) {
        this.agent = agent;
        this.relation = relation;
    }
    
    public String getAgent() {
        return agent;
    }
    
    public void setAgent(String agent) {
        this.agent = agent;
    }
    
    public AgentRelation getRelation() {
        return relation;
    }
    
    public void setRelation(AgentRelation relation) {
        this.relation = relation;
    }
    
}
