//This class is very simple. It's only use is to store a boolean and a message for relaying user account creation status.
public class UserCreationResult {
    private final boolean success;
    private final String message;

    public UserCreationResult(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess(){
        return success;
    }

    public String getMessage(){
        return message;
    }


}
