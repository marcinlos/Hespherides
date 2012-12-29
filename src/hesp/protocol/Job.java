package hesp.protocol;

import jade.content.Concept;
import jade.util.leap.Serializable;

public class Job implements Concept, Serializable {
    private long id;
    private int cputime;
    
    public Job() {
        
    }
    
    public Job(long id, int cputime) {
        this.id = id;
        this.cputime = cputime;
    }

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getCputime() {
        return cputime;
    }

    public void setCputime(int cputime) {
        this.cputime = cputime;
    }
    
    
}
