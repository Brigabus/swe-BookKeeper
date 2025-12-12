import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class BookingInfoWindow {
    private JFrame frame;
    private booking currentBooking;
    private int currentUserID;
    private String accountType;
    private LoginHandler loginHandler;
    private ScheduleHandler scheduleHandler;

    //Similar to our other smaller windows, this was largely a frankenstein of copy pasting from existing windows.
    //The biggest issue we had with this window was all the information we had to feed it to get it working the way we wanted.
    //If we had a chance to start this project over, we would have designed a system wherein loginHandler and lloginSchedule would'nt have to be
    //passed between different classes so often. (It's really ugly)
    public BookingInfoWindow(int currentUserID, String accountType, booking booking, ScheduleHandler scheduleHandler) {
        this.currentBooking = booking;
        this.currentUserID = currentUserID;
        this.accountType = accountType;
        this.loginHandler = new LoginHandler();
        this.scheduleHandler = scheduleHandler;

        frame = new JFrame("Booking Information");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        setupLayout();

        frame.setVisible(true);
    }

    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Event Name:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(currentBooking.getEventName()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(currentBooking.getRoomNum())), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(currentBooking.getStartTime().format(fmt)), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(currentBooking.getEndTime().format(fmt)), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Created By:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(currentBooking.getAuthor()), gbc);

        //We only want to let the user modify the booking if they a) own it, or b) are an admin. Otherwise we won't show the button.
        if (currentBooking.getAuthor().equals(loginHandler.getUserName(currentUserID)) || accountType.equals("Admin")) {
            JButton updateButton = new JButton("Modify Booking");
            updateButton.addActionListener(e -> new UpdateBookingWindow(currentBooking, scheduleHandler));
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(updateButton, gbc);
        }

        frame.add(panel);
    }
}
