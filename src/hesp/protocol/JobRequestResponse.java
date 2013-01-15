package hesp.protocol;

/**
 * Response for job request sent to production agent
 * 
 * @author marcinlos
 */
public class JobRequestResponse {

    private long jobId;
    private boolean accepted;
    private String details;
    
    public JobRequestResponse(long jobId, boolean accepted) {
        this.jobId = jobId;
        this.accepted = accepted;
    }
    
    public JobRequestResponse(long jobId, boolean accepted, String details) {
        this(jobId, accepted);
        this.details = details;
    }
    
    public long getJobId() {
        return jobId;
    }
    
    public void setJobId(long jobId) {
        this.jobId = jobId;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    
}
