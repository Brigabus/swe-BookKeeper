import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class AccountManageTab extends JPanel{
    int userID;
    String username;
    String accountType;
    LoginHandler loginHandler;

    public AccountManageTab(int userID){
        this.loginHandler = new LoginHandler();
        this.userID = userID;
        this.username = loginHandler.getUserName(userID);
        this.accountType = loginHandler.getUserType(userID);



    }
}
