import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;
import java.util.Date;

//This window is very similar to our create booking window and booking info windows
public class UpdateBookingWindow {
    private JFrame frame;
    private JComboBox<Integer> roomField;
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JTextField eventNameField;
    private JButton updateButton;
    private JButton deleteButton;

    private booking bookingToUpdate;
    private ScheduleHandler scheduleHandler;

    private static final Integer[] availableRooms = ScheduleWindow.availableRooms;

    public UpdateBookingWindow(booking bookingToUpdate, ScheduleHandler scheduleHandler) {
        this.bookingToUpdate = bookingToUpdate;
        this.scheduleHandler = scheduleHandler;

        frame = new JFrame("Update Booking: " + bookingToUpdate.getEventName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(450, 300);
        frame.setLocationRelativeTo(null);

        roomField = new JComboBox<>(availableRooms);
        roomField.setSelectedItem(bookingToUpdate.getRoomNum());

        //We updated this window to use the spinners as well, like we did in the new bookings window
        Date startDate = Date.from(bookingToUpdate.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        startSpinner = new JSpinner(new SpinnerDateModel(startDate, null, null, java.util.Calendar.MINUTE));
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm"));

        Date endDate = Date.from(bookingToUpdate.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        endSpinner = new JSpinner(new SpinnerDateModel(endDate, null, null, java.util.Calendar.MINUTE));
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm"));

        eventNameField = new JTextField(bookingToUpdate.getEventName(), 20);

        updateButton = new JButton("Update Booking");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //We had a lot more issues when updating our bookings as opposed to creating them, so we added a second layer of
                    //checks, alongside those in our ScheduleHandler class.
                    int roomNum = (Integer) roomField.getSelectedItem();
                    LocalDateTime newStart = LocalDateTime.ofInstant(((Date) startSpinner.getValue()).toInstant(), ZoneId.systemDefault());
                    LocalDateTime newEnd = LocalDateTime.ofInstant(((Date) endSpinner.getValue()).toInstant(), ZoneId.systemDefault());
                    String newEventName = eventNameField.getText();

                    if (newStart.isAfter(newEnd) || newStart.isEqual(newEnd)) {
                        JOptionPane.showMessageDialog(frame, "Start time must be before end time.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean success = scheduleHandler.updateBooking(bookingToUpdate, roomNum, newStart, newEnd, newEventName);

                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Booking updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Cannot update booking. Overlapping booking exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        //The delete function was one of the last things we added to the project
        deleteButton = new JButton("Delete Booking");
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this booking?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = scheduleHandler.deleteBooking(bookingToUpdate);
                if (success) {
                    JOptionPane.showMessageDialog(frame, "Booking deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to delete booking.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setupLayout();
        frame.setVisible(true);
    }

    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Event Name:"), gbc);
        gbc.gridx = 1;
        panel.add(eventNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        panel.add(roomField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        panel.add(startSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        panel.add(endSpinner, gbc);

        // Add both buttons in a panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        frame.add(panel);
    }
}
