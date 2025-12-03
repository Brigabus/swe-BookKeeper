import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//This class is largely a copy of the LoginWindow class, with few key exceptions
public class newUserWindow {
    private JFrame frame;
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField passAuthField; //We'll add a password authentication field to avoid accidental typos that lock users out of their account
    private JTextField emailField; //unused for now
    private JComboBox accountTypeField; //We'll use a combobox dropdown for specifying user type, keeping it to our three pre-defined types
    private JButton createUserButton; //Similar to our log in button on the main window, but not set to default to avoid accidental early clicks
    private LoginHandler loginHandler = new LoginHandler();

    public newUserWindow(){
        frame = new JFrame("Create New User");
        usernameField = new JTextField(20);
        passwordField = new JTextField(20);
        passAuthField = new JTextField(20);
        emailField = new JTextField(50);
        createUserButton = new JButton("Create User");

        //This is where we specify the various types of strings that can be set as account types. it's important that these are predefined to avoid unknown user types.
        String[] accountTypes = {"Student", "Teacher", "Admin"};
        accountTypeField = new JComboBox(accountTypes);

        createUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                //Why doesn't this function return a string if it uses a string array??
                String accountType = (String) accountTypeField.getSelectedItem();

                //First, we'll check to make sure the entered passwords are the same. If so, we'll move on to the account creation step using our loginHandler class.
                if(password.equals(passAuthField.getText())){
                    /*This was originally a boolean check for if the account was created or not, but I wanted to change it as a boolean wasn't able to provide a reason for creation failure
                    Instead, I created a seperate class to serve as a container for supplying a boolean and message in a single return variable.
                    While it's not perfect and still only displays a difficult to read output of the sql exception on failure, it's better than nothing.
                    This lets us check the boolean success value and the message independently for each success / failure!
                    */
                    UserCreationResult ucr = loginHandler.createUser(username, password, accountType);
                    if(ucr.isSuccess()){
                        JOptionPane.showMessageDialog(frame,ucr.getMessage(), "ACCOUNT CREATION SUCCESSFUL!", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                    }else{
                        JOptionPane.showMessageDialog(frame, "Account creation failed!\n" + ucr.getMessage(), "ACCOUNT CREATION FAILED!", JOptionPane.ERROR_MESSAGE);

                    }
                } else{
                    JOptionPane.showMessageDialog(frame, "Passwords do not match!");
                }

            }
        });

        setupLayout();

    }

    private void setupLayout(){
        JPanel nuPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nuPanel.add(new JLabel("Create new user:"), gbc);

        gbc.gridy = 1;
        nuPanel.add(new JLabel("Username: "), gbc);

        gbc.gridx = 1;
        nuPanel.add(usernameField, gbc);

        gbc.gridx = 2;
        nuPanel.add(accountTypeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        nuPanel.add(new JLabel("Password: "), gbc);
        gbc.gridx = 1;
        nuPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        nuPanel.add(new JLabel("Confirm Password: "), gbc);
        gbc.gridx = 1;
        nuPanel.add(passAuthField, gbc);

        gbc.gridy = 4;
        nuPanel.add(createUserButton, gbc);

        frame.add(nuPanel);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
