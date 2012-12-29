package hesp.protocol;

public class Transfer {

    public final long id;
    public final String sourceAccount;
    public final String targetAccount;
    public final int amount;
    
    public Transfer(long id, String sourceAccount, String targetAccount, 
            int amount) {
        this.id = id;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
    }

}
