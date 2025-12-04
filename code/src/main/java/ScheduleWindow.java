import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    //This is the window constructor. It should only ever be called by the loginWindow class after the user has already logged in.
    public ScheduleWindow(int userID, Point windowLocation){
        //We'll instantiate a loginHandler for this window. It won't be used for authentication, but instead for information gathering.
        this.loginHandler = new LoginHandler();
        this.userID = userID;
        this.username = loginHandler.getUserName(userID);
        this.accountType = loginHandler.getUserType(userID);

        this.accountManageTab = new AccountManageTab(userID);

        if(this.accountType.equals("Teacher") || this.accountType.equals("Admin")){
            this.manageBookingsTab = new ManageBookingsTab();
            tabPane.addTab("Manage Bookings", null, manageBookingsTab, "Manage Bookings");
        }
        if(this.accountType.equals("Admin")){
            this.adminActionsTab = new AdminActionsTab();
            tabPane.addTab("Admin Actions", null, adminActionsTab, "Admin Actions");
        }
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

        //Finally, we'll call the schedule updater to populate the schedule upon initial frame load.
        updateSchedule(LocalDate.now(), this);
    }

    //This small function returns the previous sunday of any date given to it. We'll use this to orient a date for scheduling.
    private LocalDate toWeekStart(LocalDate newDate){
        return newDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

    /*This function takes in a date and passes it down to the schedule generator and schedule view generator.
    This functionality was originally inside the main window constructor, but being able to access it seperately
    without needing to create a whole new window was needed later on, so it was moved to it's own function.
     */
    public void updateSchedule(LocalDate date, ScheduleWindow window){
        System.out.println("updating schedule to the week of " + date);
        //We only want to remove the current tab if one already exists.
        //This has a bit of a botched dependency on a different variable instead of the pane itself, but I was too lazy to update it and it still works.
        if(currentSchedule != null){
            tabPane.removeTabAt(0);
        }
        ScheduleView schedule = new ScheduleView(toWeekStart(date), window);
        currentSchedule = schedule;

        //We'll update the tab at index 0 (our schedule tab), then revalidate and repaint the frame to show our updates.
        tabPane.insertTab("Schedule View", null, schedule, "Schedule View", 0 );
        tabPane.setSelectedIndex(0);
        frame.revalidate();
        frame.repaint();
    }

}

/*
Timeslot is it's own seperate class so that it can be modified in the future. Upon initial creation, its only function is
to create nice looking panels for each timeslot, but eventually it will be updated to hold event functionality on a clicklistener
 */
class TimeSlot extends JPanel{

    private final LocalDate date;
    private final LocalTime time;
    private static final DateTimeFormatter FORMAT_12H = DateTimeFormatter.ofPattern("h a");

    public TimeSlot(LocalDate date, LocalTime time){
        this.date = date;
        this.time = time;

        setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        setBackground(Color.white);

        String labelText = time.format(FORMAT_12H);
        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 10));

        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
    }

}

//ScheduleGrid is the main body of the schedule. it takes the individual timeslot panels and arranges them accordingly.
class ScheduleGrid extends JPanel{
    private final LocalDate startOfWeek;
    private final int days = 7;
    private final int hours = 8;

    public ScheduleGrid(LocalDate startOfWeek){
        this.startOfWeek = startOfWeek;

        setLayout(new GridLayout(hours, days));
        buildGrid();
    }

    private void buildGrid(){
        for(int hour = 9; hour < 17; hour++){
            for(int day = 0; day < days; day++){
                LocalDate date = startOfWeek.plusDays(day);
                LocalTime time = LocalTime.of(hour, 0);

                TimeSlot slot = new TimeSlot(date, time);
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

    public ScheduleView(LocalDate startDate, ScheduleWindow window){
        this.window = window;
        this.grid = new ScheduleGrid(startDate);
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
        JPanel panel = new JPanel(new GridLayout(1, 2));
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
                window.updateSchedule(startDate.plusDays(7), window);
            }
        });

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.updateSchedule(startDate.minusDays(7), window);
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