import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

//This tab will allow the user to see all of their created bookings (or all booking period if they're an admin)
//Fun fact: we didn't have this originally, but wanted to add it after we added them everywhere in our databases project
public class ManageBookingsTab extends JPanel {
    private int userID;
    private String username;
    private String accountType;
    //once again passing down loginhandler and schedulehander :(
    private LoginHandler loginHandler;
    private ScheduleHandler scheduleHandler;

    //We're using JTable for obvious reasons here
    private JTable bookingsTable;
    private DefaultTableModel tableModel;

    public ManageBookingsTab(int userID, ScheduleHandler scheduleHandler) {
        this.userID = userID;
        this.scheduleHandler = scheduleHandler;
        this.loginHandler = new LoginHandler();
        this.username = loginHandler.getUserName(userID);
        this.accountType = loginHandler.getUserType(userID);

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createBookingButton = new JButton("Create New Booking");
        topPanel.add(createBookingButton);


        //The original plan was to have this page update automatically upon selection, but that would require us to reference the original ScheduleWindow
        //that it came from which meant passing down MORE things, so we just decided to add a button instead for the sake of cleanliness and time.
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBookings();
            }
        });
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);


        String[] columns = {"Room", "Event Name", "Start Time", "End Time", "Author"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //AI helped with this part, we wanted to stop the cells from being editable, so it gave us this nifty function and it worked
                return false;
            }
        };
        bookingsTable = new JTable(tableModel);
        //We add a scrollPane here, just in case the list gets really long.
        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        add(scrollPane, BorderLayout.CENTER);


        createBookingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new NewBookingWindow(username, userID);
            }
        });

        bookingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                //Originally we had a simple button to make changes to table entries, but that looked ugly, so we changed it to a double click instead.
                if (evt.getClickCount() == 2) {
                    int row = bookingsTable.getSelectedRow();
                    if (row >= 0) {

                        int roomNum = (int) tableModel.getValueAt(row, 0);
                        String eventName = (String) tableModel.getValueAt(row, 1);
                        LocalDateTime startTime = LocalDateTime.parse((String) tableModel.getValueAt(row, 2), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        LocalDateTime endTime = LocalDateTime.parse((String) tableModel.getValueAt(row, 3), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));


                        booking selectedBooking = scheduleHandler.getBooking(roomNum, startTime);
                        if (selectedBooking != null) {
                            new UpdateBookingWindow(selectedBooking, scheduleHandler);
                        }
                    }
                }
            }
        });

        //Finally we'll refresh the bookings table to make sure everything is rendered upon first creation
        refreshBookings();
    }

    //This works a bit differently to our scheduleView refresh method. There we simply repaint the whole frame, but we can't really do that
    //here given it's a seperate panel, so we opted to just remake the whole table upon refresh instead.
    public void refreshBookings() {
        tableModel.setRowCount(0);

        List<booking> bookings;
        if (accountType.equals("Admin")) {
            bookings = scheduleHandler.getAllBookings();
        } else {
            bookings = scheduleHandler.getBookingsByAuthor(username);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (booking b : bookings) {
            tableModel.addRow(new Object[]{
                    b.getRoomNum(),
                    b.getEventName(),
                    b.getStartTime().format(fmt),
                    b.getEndTime().format(fmt),
                    b.getAuthor()
            });
        }
    }
}
