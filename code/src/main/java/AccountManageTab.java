import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//This class will be used to create the account management tab in our final gui
//it should allow the user to change their username and password
public class AccountManageTab extends JPanel {
    private int userID;
    private String username;
    private String accountType;
    private LoginHandler loginHandler;

    private JTextField usernameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton saveButton;

    //When constructing the window, we'll want to get the user's ID. This is done after the user logs in, so it should be fine.
    public AccountManageTab(int userID) {
        //most of this stuff is copy pasted from other areas of the code and frankenstein'd into something new and useful!
        this.loginHandler = new LoginHandler();
        this.userID = userID;
        this.username = loginHandler.getUserName(userID);
        this.accountType = loginHandler.getUserType(userID);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Update Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(username, 20);
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        add(newPasswordField, gbc);

        //We'll have the user confirm and changes they make to their password to avoid having them getting locked out of their account by accident
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        saveButton = new JButton("Save Changes");
        add(saveButton, gbc);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });
    }

    private void saveChanges() {
        String newUsername = usernameField.getText();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        //Here, we check what HAS and HASN'T been changed. if a user hasn't changed anything, we can safely assume they don't want to change it
        //So we'll update it to whatever it was set to before
        boolean usernameChanged = !newUsername.equals(username);
        boolean passwordChanged = !newPassword.isEmpty();

        //We can also use the ! operator to check if the new passwords match, and stop the user from changing them if they don't.
        if (passwordChanged && !newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //If we detect that NO changes have been made, we can avoid doing any updates altogether (but also let the user know)
        if (!usernameChanged && !passwordChanged) {
            JOptionPane.showMessageDialog(this, "No changes to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //We'll check if the loginHandler was able to successfully make our changes
        boolean success = loginHandler.updateUser(userID, newUsername, passwordChanged ? newPassword : null);

        //Either way we'll let the user know
        //We'll also update the edit fields and clear them out to help demonstrate that the update registered.
        if (success) {
            JOptionPane.showMessageDialog(this, "Account updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            username = newUsername;
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
