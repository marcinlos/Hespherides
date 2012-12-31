package hesp.agents;

/**
 * @author marcinlos
 *
 * Enumeration describing scheduled job's lifecycle.
 */
public enum JobStatus {
    /** Job is on the execution queue, not started yet */
    QUEUED,
    /** Job is being executed */
    IN_PROGRESS,
    /** Job's execution has failed */
    FAILED,
    /** Job has been successfully executed */
    SUCCEEDED
}
