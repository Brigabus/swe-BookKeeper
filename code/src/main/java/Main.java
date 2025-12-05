import javax.swing.*;

public class Main {

    public static void main(String[] args){
        System.out.println("BookKeeper Started!");
        LoginHandler loginHandler = new LoginHandler();
        LoginHandler.initialize();

        LogInWindow newLogin = new LogInWindow();
    }

}
