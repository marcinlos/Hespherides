package hesp.agents;

/**
 * Structure containing job-in-progress information. 
 */
public class JobProgress {
    
    private long id;
    private JobStatus status;
    private int required;
    private int done = 0;
    
    public JobProgress(long id, int workRequired) {
        this.id = id;
        this.required = workRequired;
        this.status = JobStatus.QUEUED;
    }
    
    public long getId() {
        return id;
    }
    
    /**
     * @return the amount (in abstract units) of work that needs to be done
     * in order to execute this job
     */
    public int getWorkRequired() {
        return required;
    }
    
    /**
     * @return the amount (in abstract units) of work that has already been
     * done
     */
    public int getWorkDone() {
        return done;
    }
    
    public JobStatus getStatus() {
        return status;
    }
    
    public boolean hasSucceeded() {
        return status == JobStatus.SUCCEEDED;
    }
    
    public boolean hasFailed() {
        return status == JobStatus.FAILED;
    }
    
    /**
     * Causes the job to fail instantly.
     */
    public void fail() {
        status = JobStatus.FAILED;
    }
    
    /**
     * Call this before updating job.
     */
    public void start() {
        if (status == JobStatus.QUEUED) {
            this.status = JobStatus.IN_PROGRESS;
        }
    }
    
    /**
     * Updates the job. 
     */
    public void update() {
        if (++ done == required) {
            this.status = JobStatus.SUCCEEDED;
        }
    }
    
    /**
     * @return percent of work done
     */
    public double getCompletionPercent() {
        return 100.0 * done / required;
    }
    
}