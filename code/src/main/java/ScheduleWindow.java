import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;


//This class handles the main Schedule viewing window. The java file also contains some additional smaller classes that are used to create the window.
public class ScheduleWindow {
    public JFrame frame;
    private JTabbedPane tabPane = new JTabbedPane();
    private ScheduleView currentSchedule;
    private LoginHandler loginHandler;
    private int userID;
    private String username;
    private String accountType;
    private AccountManageTab accountManageTab;
    private AdminActionsTab adminActionsTab;
    private ManageBookingsTab manageBookingsTab;
    private JPanel roomSwitchPanel;
    private GridBagConstraints gbc;
    public ScheduleHandler scheduleHandler;
    private LocalDate currentStartDate;
    private JComboBox roomPicker;
    public static Integer[] availableRooms = {301, 302, 303, 304};
    public int currentRoomNum = availableRooms[0];
    private JLabel scheduleLabel;

    //This is the window constructor. It should only ever be called by the loginWindow class after the user has already logged in.
    public ScheduleWindow(int userID, Point windowLocation){
        //We'll instantiate a loginHandler for this window. It won't be used for authentication, but instead for information gathering.
        this.loginHandler = new LoginHandler();
        this.scheduleHandler = new ScheduleHandler();
        this.userID = userID;
        this.username = loginHandler.getUserName(userID);
        this.accountType = loginHandler.getUserType(userID);

        //Here we'll add tabs depending on the account Type of the user.
        this.accountManageTab = new AccountManageTab(userID);

        if(this.accountType.equals("Teacher") || this.accountType.equals("Admin")){
            this.manageBookingsTab = new ManageBookingsTab();
            tabPane.addTab("Manage Bookings", null, manageBookingsTab, "Manage Bookings");
        }
        if(this.accountType.equals("Admin")){
            this.adminActionsTab = new AdminActionsTab();
            tabPane.addTab("Admin Actions", null, adminActionsTab, "Admin Actions");
        }
        //All users have access to the account management tab
        tabPane.addTab("Account Management",null, accountManageTab, "Account Management");

        //Here we'll create our frame for the main application.
        frame = new JFrame("BookKeeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 768);
        frame.setLocation(windowLocation);
        frame.setLayout(new BorderLayout(10, 20));
        frame.add(tabPane, BorderLayout.CENTER);
        frame.setVisible(true);

        //We'll add a top panel containing important information that the user might want at a glance.
        frame.add(new TopPanel(userID, username, accountType, this), BorderLayout.NORTH);

        //Here we'll set up a ComboBox to allow the user to switch the schedule view to different rooms.
        //The available rooms are dictated by an array in this class. Ideally it should be set to a seperate database, but I set it up like this in the interest of time.
        this.roomPicker = new JComboBox<>(availableRooms);
        roomPicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Actionlisteners can't pass down a this, which is needed for updating the schedule window, so we'll pass it to it's own function instead.
                PickRoom((Integer) roomPicker.getSelectedItem());
            }
        });


        //The room Switch panel houses our schedule and the controls for switching through available rooms.
        //These controls were originally created with the nav buttons, but that introduced problems when recreating them for a new scheduleview.
        roomSwitchPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        scheduleLabel = new JLabel();
        setupSchedulePanel();

        tabPane.insertTab("Schedule View", null, roomSwitchPanel, "Schedule View", 0 );
        //Finally, we'll call the schedule updater to populate the schedule upon initial frame load.
        updateSchedule(currentRoomNum, LocalDate.now(), this);
    }

    //This small function returns the previous sunday of any date given to it. We'll use this to orient a date for scheduling.
    private LocalDate toWeekStart(LocalDate newDate){
        return newDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

    }

    /*This function takes in a date and passes it down to the schedule generator and schedule view generator.
    This functionality was originally inside the main window constructor, but being able to access it seperately
    without needing to create a whole new window was needed later on, so it was moved to it's own function.
     */
    public void updateSchedule(int roomNum, LocalDate date, ScheduleWindow window){
        System.out.println("updating room " + currentRoomNum + "schedule to the week of " + date);
        //We only want to remove the current tab if one already exists.
        //This has a bit of a botched dependency on a different variable instead of the pane itself, but I was too lazy to update it and it still works.
        currentStartDate = toWeekStart(date);
        ScheduleView schedule = new ScheduleView(currentStartDate, window, roomNum, scheduleHandler);
        if(currentSchedule != null){
            roomSwitchPanel.remove(currentSchedule);
        }
        currentSchedule = schedule;

        //Here we specify the gbc constrains for adding in our schedule.
        //While this doesn't need to be done each time we update the schedule, it helps to avoid mishaps during the update process
        gbc.gridy = 1;
        gbc.gridx=0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 9;
        gbc.weightx = 1;
        gbc.weighty = 1;
        roomSwitchPanel.add(schedule, gbc);

        scheduleLabel.setText("Room " + currentRoomNum + " - Schedule for week of " + currentStartDate + ":");

        tabPane.setSelectedIndex(0);
        //Revalidate and repaint are required for updating the GUI once changes have been made.
        frame.revalidate();
        frame.repaint();
    }

    //This function exists solely to hold code that looked messy in the main constructor.
    //All it does is set up different panels and gbc constraints to make the program look nicer.
    private void setupSchedulePanel(){
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.gridy = 0;
        //Here we'll set up each of our panels for the top layer
        //Panel 0
        gbc.gridx = 0;
        gbc.gridwidth=1;
        JLabel roomPickerLabel = new JLabel("Room: ");
        roomSwitchPanel.add(new JPanel().add(roomPickerLabel), gbc);

        //Panel 1
        gbc.gridx = 1;
        gbc.weightx=1;
        roomSwitchPanel.add(roomPicker, gbc);

        //panel 2
        gbc.gridx = 2;
        gbc.gridwidth = 5;
        roomSwitchPanel.add(new JPanel().add(scheduleLabel), gbc);

        //panel 8
        gbc.gridx=8;
        gbc.gridwidth = 1;
        roomSwitchPanel.add(new JPanel().add(new JLabel(" ")), gbc);

    }

    //This is the function where we outsource the roomPicker update to so we can pass down the window variable as this.
    private void PickRoom(int roomNum){
        currentRoomNum = roomNum;
        updateSchedule(currentRoomNum, currentStartDate, this);
    }

}

/*
Timeslot is it's own seperate class so that it can be modified in the future. Upon initial creation, its only function is
to create nice looking panels for each timeslot, but eventually it will be updated to hold event functionality on a clicklistener
 */
class TimeSlot extends JPanel{

    private final LocalDate date;
    private final LocalTime time;
    private final int roomNum;
    private booking slotBooking;
    private static final DateTimeFormatter FORMAT_12H = DateTimeFormatter.ofPattern("h a");

    public TimeSlot(int roomNum, LocalDate date, LocalTime time, ScheduleHandler scheduleHandler){
        this.date = date;
        this.time = time;
        this.roomNum = roomNum;
        LocalDateTime dateTime = date.atTime(time);

        //Upon creation, every timeslot will check the database schedule to see if an event is currently planned within it's time at the specified room.
        this.slotBooking = scheduleHandler.getBooking(roomNum, dateTime);

        JLabel label = new JLabel();

        if(slotBooking != null){
            //If a booking is found we can change the look and function of the slot and populate it with info about out booking
            System.out.println("Booking Found");
            setBorder(BorderFactory.createLineBorder(Color.green, 1));
            setBackground(Color.green);

            label.setText(slotBooking.eventName);
            label.setFont(new Font("Arial", Font.PLAIN, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("<html>" + slotBooking.eventName + "<br>" + slotBooking.timeStart.toString() + " to " + slotBooking.timeEnd.toString() + "<br>Created by: " + slotBooking.authorName + "</html>");
            //We'll add a mouse listener so the user can click on the booking to bring up more information about it, or make changes if possible
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    System.out.println("Clicked on event");
                }
            });

        }else{
            //if no booking is found, we can set the timeslot to be empty
            setBorder(BorderFactory.createLineBorder(Color.gray, 1));
            setBackground(Color.white);

            String labelText = time.format(FORMAT_12H);
            label.setText(labelText);
            label.setFont(new Font("Arial", Font.PLAIN, 10));
        }
        //I'm sure there is a more efficient way to check for timeslots, as opposed to polling the database for every hour of a week, but due to time constraints
        //I decided to just use this method because it works.

        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
    }

}

//ScheduleGrid is the main body of the schedule. it takes the individual timeslot panels and arranges them accordingly.
class ScheduleGrid extends JPanel{
    private final LocalDate startOfWeek;
    private final int days = 7;
    private final int hours = 8;
    private int roomNum;
    private ScheduleHandler scheduleHandler;

    public ScheduleGrid(int roomNum, LocalDate startOfWeek, ScheduleHandler scheduleHandler){
        this.roomNum = roomNum;
        this.startOfWeek = startOfWeek;
        this.scheduleHandler = scheduleHandler;

        setLayout(new GridLayout(hours, days));
        buildGrid();
    }

    private void buildGrid(){
        for(int hour = 9; hour < 17; hour++){
            for(int day = 0; day < days; day++){
                LocalDate date = startOfWeek.plusDays(day);
                LocalTime time = LocalTime.of(hour, 0);

                TimeSlot slot = new TimeSlot(roomNum, date, time, scheduleHandler);
                add(slot);
            }
        }
    }

}

//ScheduleView is similar to the scheduleGrid class. it takes the scheduleGrid and adds headers and navigation buttons.
class ScheduleView extends JPanel{
    private final ScheduleGrid grid;
    private final LocalDate startDate;
    private final int hours = 9;
    private ScheduleWindow window;
    private int currentRoomNum;

    public ScheduleView(LocalDate startDate, ScheduleWindow window, int roomNum, ScheduleHandler scheduleHandler){
        this.window = window;
        this.grid = new ScheduleGrid(roomNum, startDate, scheduleHandler);
        this.startDate = startDate;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(createNavButtons(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createDayHeader(), gbc);

        //This is where we add the actual grid of timeslots
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(grid, gbc);

        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        add(createHourHeader(), gbc);

    }

    //The createDay and createHour header functions took the most time to get right (annoyingly)
    //I ended up getting frustrated with them, so I eventually rewrote them to both just use a seperate HeaderCell panel creation function.
    private JPanel createDayHeader(){
        JPanel panel = new JPanel(new GridLayout(1, 7));
        for(int i = 0; i < 7; i++){
            LocalDate date = startDate.plusDays(i);
            String label1 = date.getDayOfWeek().toString();
            String label2 = date.getDayOfMonth() + " " + date.getMonth();

            panel.add(new headerCell(new String[]{label1, label2}));
        }

        return panel;
    }

    private JPanel createHourHeader(){
        JPanel panel = new JPanel(new GridLayout(8, 1));
        //panel.add(new JLabel("", SwingConstants.CENTER));
        for(int i = 9; i < 17; i++){
            LocalTime time = LocalTime.of(i, 0);
            String label = time.format(DateTimeFormatter.ofPattern("h a")) + " - " + LocalTime.of(i+1, 0).format(DateTimeFormatter.ofPattern("h a"));

            //panel.add(new JLabel(label, SwingConstants.CENTER));
            panel.add(new headerCell(new String[]{label}));
        }

        return panel;
    }

    //This is where we create the buttons that navigate forward and backwards in the week.
    private JPanel createNavButtons(){
        JPanel panel = new JPanel(new GridLayout(1, 3));
        JButton nextButton = new JButton(">>>");
        JButton prevButton = new JButton("<<<");


        /*
        These actionlisteners each run the same updateSchedule function as the window constructor (which is why the window needs to be passed down to them)
        All they do is update the schedule with the current date +/- 7 days.
        These buttons are the reason that updateSchedule exists, instead of it being in the window constructor.
         */
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.updateSchedule(window.currentRoomNum, startDate.plusDays(7), window);
            }
        });

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.updateSchedule(window.currentRoomNum, startDate.minusDays(7), window);
            }
        });

        panel.add(prevButton);
        panel.add(nextButton);
        return panel;
    }
}

//This headercell class wasn't needed, but it helped to make the program less ugly. The function itself is ugly instead :)
//All it does is wrap an array of strings into as many lines as needed. None of Swing's built in functions did it in a way I liked.
class headerCell extends JPanel{
    String[] strings;

    public headerCell(String[] strings){
        this.strings = strings;

        setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        setBackground(Color.white);
        setLayout(new GridLayout(strings.length,1));

        for(int i = 0; i < strings.length; i++){
            JLabel label = new JLabel(strings[i], SwingConstants.CENTER);
            if(i == 0){
                label.setFont(new Font("Arial", Font.BOLD, 12));
            }else{
                label.setFont(new Font("Arial", Font.PLAIN, 10));
            }

            if(strings.length == 1){
                setLayout(new BorderLayout());
                add(label, BorderLayout.CENTER);
            }else{
                add(label);
            }
        }

    }
}

//The topPanel class is created to add some at-a-glance information that the user might want, such as username and account type.
// it can be expanded in the future if needed.
class TopPanel extends JPanel{
    int accountID;
    String accountName;
    String accountType;

    public TopPanel(int accountID, String accountName, String accountType, ScheduleWindow window){
        this.accountID = accountID;
        this.accountName = accountName;
        this.accountType = accountType;

        setLayout(new BorderLayout());

        JPanel westPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel nameLabel = new JLabel(accountName);
        JLabel accountLabel = new JLabel(accountType + " account:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        gbc.weighty=1;
        gbc.insets = new Insets(5, 5, 5, 5);

        westPanel.add(accountLabel, gbc);
        gbc.gridy = 1;
        westPanel.add(nameLabel, gbc);

        add(westPanel, BorderLayout.WEST);

        JPanel eastPanel = new JPanel(new GridBagLayout());
        gbc.gridy = 0;
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LogInWindow newLogin = new LogInWindow();
                window.frame.dispose();
            }
        });
        eastPanel.add(logoutButton, gbc);

        add(eastPanel, BorderLayout.EAST);


    }

}

class EventInfoWindow{
    booking currentBooking;

    public EventInfoWindow(booking viewBooking){
        this.currentBooking = viewBooking;

    }

}