package hesp.protocol;

/**
 * Structure containing all the parameters describing a job.
 * 
 * @author marcinlos
 */
public class JobParameters {
    private int cputime;

    public JobParameters(int cputime) {
        this.cputime = cputime;
    }

    public int getCputime() {
        return cputime;
    }

    public void setCputime(int cputime) {
        this.cputime = cputime;
    }
}