package hesp.protocol;

/**
 * Money transfer request message
 * 
 * @author marcinlos
 */
public class Transfer {

    /**
     * Transaction identifier
     */
    public final long id;
    
    /**
     * Target to withdraw money from
     */
    public final String sourceAccount;
    
    /**
     * Target to add money to
     */
    public final String targetAccount;
    
    /**
     * Amount of money to transfer
     */
    public final int amount;
    
    public Transfer(long id, String sourceAccount, String targetAccount, 
            int amount) {
        this.id = id;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
    }

}
