package hesp.protocol;


public class Job {
    private long id;
    
    private JobParameters parameters;// = new JobParameters();

    public Job(long id, JobParameters params) {
        this.id = id;
        this.parameters = params;
    }

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public JobParameters getParameters() {
        return parameters;
    }

    /**
     * Utility method to generate reasonably likely unique job identifiers.
     * 
     * @param source Object requesting unique id
     * @param seed Random integral value
     * @return unique (with high probability) job identifier
     */
    public static long generateId(Object source, long seed) {
        return System.nanoTime() ^ source.hashCode() + seed;
    }
}
