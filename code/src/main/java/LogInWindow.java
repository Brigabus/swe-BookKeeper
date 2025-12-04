import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogInWindow {
    private JFrame frame;
    private JLabel namePlate;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton newUserButton;
    //We'll initialize an instance of our loginHandler class to manage the authentications of our logins using SQLite.
    private LoginHandler loginHandler = new LoginHandler();

    //We'll set up an all-in-one constructor for our login window class. this will construct and display the login window when called.
    public LogInWindow(){
        //We'll initialze our different components here to add to our login window.
        frame = new JFrame("BookKeeper Login");
        namePlate = new JLabel("BookKeeper");
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");

        //We can add an actionListener to our login button that tries to authenticate the user when clicked
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if(loginHandler.authenticate(username, password)){
                    System.out.println("Successful Login!");
                    loginUser(username);
                    frame.dispose();
                } else{
                    System.out.println("Invalid Login!");
                    JOptionPane.showMessageDialog(frame, "Invalid Login!", "INVALID LOGIN!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //We'll also need to add a button for adding new users to our database
        newUserButton = new JButton("Create User");
        newUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewUser();
            }
        });


        //We can then call the setupLayout function so that a new window is opened whenever the constructor is called
        setupLayout();
    }

    //This function simply adds all of our components to a panel and displays that panel on a frame
    private void setupLayout(){
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);


        namePlate.setFont(new Font("Arial", Font.BOLD, 32));
        gbc.fill = GridBagConstraints.HORIZONTAL;

        loginPanel.add(new JPanel());
        gbc.gridx = 1;
        gbc.gridy = 0;
        loginPanel.add(namePlate, gbc);

        gbc.gridy = 1;
        loginPanel.add(new JPanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Username"), gbc);

        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        loginPanel.add(loginButton, gbc);
        //This is a small QOL feature that sets the default button for the login page as the loginbutton so that pressing enter triggers the button.
        frame.getRootPane().setDefaultButton(loginButton);

        gbc.gridy = 6;
        loginPanel.add(newUserButton, gbc);

        frame.add(loginPanel);
        frame.setSize(900, 768);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    //This is the function that is run when user login is successful. It will do some small things, then move the user on to the next program state, depending on the user account type.
    private void loginUser(String username){
        int userID = loginHandler.getUserID(username);
        String userType = loginHandler.getUserType(userID);
        System.out.println("ID: " + userID + " Account Type: " + userType);
        JOptionPane.showMessageDialog(frame, "Welcome " + username + "!\n Account Type: " + userType, "LOGIN SUCCESSFUL!", JOptionPane.INFORMATION_MESSAGE);

        //Now that the user has successfully logged in, we can create the main schedule view window, while also sending along the already authenticated userID.
        //We're also sending over the location of the login frame to make it a bit more seamless when switching frames to the main application window.
        ScheduleWindow window = new ScheduleWindow(userID, frame.getLocation());
    }

    //This function is called to bring up the user creation window, as part of the newUserWindow class.
    private void createNewUser(){

        newUserWindow createWindow = new newUserWindow();
        //frame.dispose();
    }

}
