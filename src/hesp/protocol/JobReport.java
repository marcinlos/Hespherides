package hesp.protocol;

import jade.util.leap.Serializable;

public class JobReport implements Serializable {
    
    private long jobId;
    private boolean status;
    private String description;
    
    public JobReport() {
        
    }
    
    public JobReport(long jobId, boolean status, String description) {
        this.jobId = jobId;
        this.status = status;
        this.description = description;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    

}
