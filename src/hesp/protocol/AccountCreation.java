package hesp.protocol;

import java.util.ArrayList;
import java.util.List;

public class AccountCreation {

    public final String owner;
    public final List<String> users;
    
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
