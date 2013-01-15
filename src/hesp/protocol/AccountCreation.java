package hesp.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure containing information required by the bank from the client
 * during creation of an account 
 * 
 * @author marcinlos
 */
public class AccountCreation {

    /**
     * Agent owning the account
     */
    public final String owner;
    
    /**
     * Agents (other than owner) authorized to withdraw/deposit money 
     * to this account
     */
    public final List<String> users;
    
    /**
     * Creates an account creation request with empty users list
     * 
     * @param owner Owner of an account
     */
    public AccountCreation(String owner) {
        this.owner = owner;
        this.users = new ArrayList<>();
    }
    
    public AccountCreation(String owner, String user) {
        this.owner = owner;
        users = new ArrayList<>();
        users.add(user);
    }
    
}
