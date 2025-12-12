import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Calendar;

//This window allows the user to specify information for a new booking, then uses the scheduleHandler to enter that data into a database table entry
public class NewBookingWindow {
    //Originally we used textboxes to modify all the data of the bookings, but that led to formatting errors
    //So instead we decided to use JSpinners which let us specify dates and times in a format that we could anticipate
    private JFrame frame;
    private JComboBox<Integer> roomDropdown;
    private JSpinner dateSpinner;
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JTextField eventField;
    private JButton createBookingButton;
    private final ScheduleHandler scheduleHandler = new ScheduleHandler();
    private final String username;

    public NewBookingWindow(String username, int userID) {
        this.username = username;

        frame = new JFrame("Create new booking for " + username);

        //Our list of rooms would probably be better off as it's own database, similar to how we did it in our databases project.
        //We had planned on implementing changes like that, but due to creeping time constraints we decided to keep our
        //placeholder method instead.
        roomDropdown = new JComboBox<>(ScheduleWindow.availableRooms);

        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);


        startSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "HH:mm");
        startSpinner.setEditor(startEditor);

        endSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "HH:mm");
        endSpinner.setEditor(endEditor);

        eventField = new JTextField(20);

        createBookingButton = new JButton("Create Booking");
        createBookingButton.addActionListener(this::createBooking);

        setupLayout();
    }

    //We created a second constructor for this class to allow users to click an empty timeslot on the scheduleview and have the information automatically
    //populate into a new booking window. That required us to pass down a lot of extra information though, so we added a second constructor to handle that.
    public NewBookingWindow(String username, int userID, String accountType, int roomNum, LocalDateTime startDateTime, LocalDateTime endDateTime, ScheduleHandler scheduleHandler) {

        this.username = username;

        frame = new JFrame("Create new booking for " + username);


        roomDropdown = new JComboBox<>(ScheduleWindow.availableRooms);
        roomDropdown.setSelectedItem(roomNum);


        Date startDate = Date.from(startDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        dateSpinner = new JSpinner(new SpinnerDateModel(startDate, null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        //This constructor works almost identically to our original one, but after creating the spinner boxes
        //we automatically set their values to match the timeslot that the user clicked on
        dateSpinner.setEditor(dateEditor);


        Date startTime = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        startSpinner = new JSpinner(new SpinnerDateModel(startTime, null, null, Calendar.MINUTE));
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "HH:mm");
        startSpinner.setEditor(startEditor);


        Date endTime = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        endSpinner = new JSpinner(new SpinnerDateModel(endTime, null, null, Calendar.MINUTE));
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "HH:mm");
        endSpinner.setEditor(endEditor);

        eventField = new JTextField(20);


        createBookingButton = new JButton("Create Booking");
        createBookingButton.addActionListener(this::createBooking);

        setupLayout();
    }

    private void setupLayout() {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        panel.add(roomDropdown, gbc);


        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        panel.add(dateSpinner, gbc);


        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        panel.add(startSpinner, gbc);


        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        panel.add(endSpinner, gbc);


        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Event Name:"), gbc);
        gbc.gridx = 1;
        panel.add(eventField, gbc);


        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(createBookingButton, gbc);

        frame.add(panel);
        frame.setSize(400, 320);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }


    private void createBooking(ActionEvent e) {

        //We use try and catch here similar to how it's used in our ScheduleHandler's create booking system
        try {
            int roomNum = (Integer) roomDropdown.getSelectedItem();

            Date dateRaw = (Date) dateSpinner.getValue();
            Date startRaw = (Date) startSpinner.getValue();
            Date endRaw = (Date) endSpinner.getValue();


            LocalDate date = dateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalTime start = startRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime end = endRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            LocalDateTime startDT = LocalDateTime.of(date, start);
            LocalDateTime endDT   = LocalDateTime.of(date, end);

            //This is where we run our checks for overlaps and start > end
            //SQLite doesn't have as much flexibility as MYSQL, so we decided early on to bypass the limitations and just do our checks through java instead.
            if (endDT.isBefore(startDT)) {
                JOptionPane.showMessageDialog(frame, "End time must be after start time.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String eventName = eventField.getText().trim();
            if (eventName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Event name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = scheduleHandler.createBooking(
                    roomNum,
                    startDT,
                    endDT,
                    eventName,
                    username
            );

            if (success) {
                JOptionPane.showMessageDialog(frame,
                        "Booking created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();

            } else {
                JOptionPane.showMessageDialog(frame,
                        "An overlapping booking already exists.\nCannot create this booking.",
                        "Conflict",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Error creating booking. Check date/time inputs.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
