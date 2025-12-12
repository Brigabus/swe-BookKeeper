import java.sql.*;
import java.io.File;
import java.net.URISyntaxException;
import java.time.*;
import java.util.*;
import java.time.format.DateTimeFormatter;

/*Our schedule handler class will handle all queries and inserts for our bookings database.
This probably could've been added to the same database as our logins, just as another table, but
I decided to keep them in seperate files to avoid accidental screwups taking down the entire system.
 */
public class ScheduleHandler {

    //The functionality of this class is essentially copied from the loginHandler class, but the values of the database tables are changed.
    private static String DB_URL;

    public ScheduleHandler(){
        Initialize();
    }

    public static void Initialize(){

        File jarFile = getJarDirectory(Main.class);
        File jarDir = jarFile.getParentFile();

        File dbFile = new File(jarDir, "schedule.db");
        DB_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try(Connection conn = DriverManager.getConnection(DB_URL)){

            Statement stmt = conn.createStatement();

            String init = """
                    CREATE TABLE IF NOT EXISTS bookings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    roomNum INTEGER NOT NULL,
                    timeStart DATETIME NOT NULL,
                    timeEnd DATETIME NOT NULL,
                    eventName VARCHAR(30) NOT NULL,
                    author VARCHAR(20)
                    );
                    """;
            stmt.execute(init);

            //TEST VALUES
//            boolean testInsert = createBooking(314,
//                    LocalDateTime.of(2025, 12, 5, 11, 0, 0),
//                    LocalDateTime.of(2025, 12, 5, 15, 0, 0),
//                    "Test Event", "Test Author");

            System.out.println("Schedule Database initialized without error.");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static File getJarDirectory(Class<?> mainClass){
        try{
            File jarFile = new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile;
        }catch(Exception e){
            throw new RuntimeException("Unable to determine JAR directory", e);
        }
    }

    public boolean createBooking(int roomID, LocalDateTime startTime, LocalDateTime endTime, String eventName, String authorName){
        //SQLite has no way of internally checking for overlaps in the date column, so it has to be done manually when adding new bookings.
        String checkSQL = """
                            SELECT 1 FROM bookings
                            WHERE roomNum = ?
                            AND timeStart < ?
                            AND timeEnd > ?
                            LIMIT 1;
                            """;

        String insertSQL = """
                            INSERT INTO bookings(roomNum, timeStart, timeEnd, eventName, author)
                            VALUES (?, ?, ?, ?, ?);
                            """;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startStr = startTime.format(fmt);
        String endStr = endTime.format(fmt);

        try(Connection conn = DriverManager.getConnection(DB_URL)){
            PreparedStatement stmt = conn.prepareStatement(checkSQL);

            stmt.setInt(1, roomID);
            stmt.setString(2, endStr);
            stmt.setString(3, startStr);

            ResultSet rs = stmt.executeQuery();
            //If we send off a query for any current bookings found within the timeslot at the given room, if we have anything return then we know
            //an overlap would be caused by inserting here, so we can abort.
            if(rs.next()){
                // conflict found, abort
                System.out.println("Conflict found while attempting to create new booking!");
                return false;
            }else{
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL);

                insertStmt.setInt(1, roomID);
                insertStmt.setString(2, startStr);
                insertStmt.setString(3, endStr);
                insertStmt.setString(4, eventName);
                insertStmt.setString(5, authorName);

                int rowsAffected = insertStmt.executeUpdate();
                System.out.println("Database update. " + rowsAffected + " Rows affected.");
                return rowsAffected > 0;

            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        System.out.println("No bueno!");
        return false;
    }

    //While the database handles all the booking information relative to other bookings, I created a seperate booking class
    //For passing that information back and forth easily within java.
    public booking getBooking(int roomNum, LocalDateTime timeSlot){
        String sql = """
        SELECT * FROM bookings
        WHERE roomNum = ?
        AND timeStart <= ?
        AND timeEnd > ?;
        """;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeStr = timeSlot.format(fmt);

        try (Connection conn = DriverManager.getConnection(DB_URL)){
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, roomNum);
            stmt.setString(2, timeStr);
            stmt.setString(3, timeStr);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                int newRoomNum = rs.getInt("roomNum");
                String newStart = rs.getString("timeStart");
                String newEnd = rs.getString("timeEnd");
                String newName = rs.getString("eventName");
                String newAuthor = rs.getString("author");
                int bookingID = rs.getInt("id");

                LocalDateTime newStartTime = LocalDateTime.parse(newStart, fmt);
                LocalDateTime newEndTime = LocalDateTime.parse(newEnd, fmt);

                return new booking(newRoomNum, newStartTime, newEndTime, newName, newAuthor, bookingID);
            }else{
                return null;
            }


        } catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    //We had to add some smaller getters as we went on for implementations of things like the manage bookings table etc.
    //These methods return lists that the bookings table can use to populate itself
    public List<booking> getAllBookings() {
        String sql = "SELECT * FROM bookings ORDER BY timeStart ASC";
        return loadBookings(sql, null);
    }

    public List<booking> getBookingsByAuthor(String author) {
        String sql = "SELECT * FROM bookings WHERE author = ? ORDER BY timeStart ASC";
        return loadBookings(sql, author);
    }

    private List<booking> loadBookings(String sql, String authorFilter) {
        List<booking> list = new java.util.ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            if(authorFilter != null)
                stmt.setString(1, authorFilter);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int roomNum = rs.getInt("roomNum");
                String start = rs.getString("timeStart");
                String end = rs.getString("timeEnd");
                String name = rs.getString("eventName");
                String author = rs.getString("author");
                int bookingID = rs.getInt("id");

                LocalDateTime startTime = LocalDateTime.parse(start, fmt);
                LocalDateTime endTime = LocalDateTime.parse(end, fmt);

                list.add(new booking(roomNum, startTime, endTime, name, author, bookingID));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    //Our updateBooking method functions similarly to our createBooking method
    public boolean updateBooking(booking oldBooking, int newRoom, LocalDateTime newStart, LocalDateTime newEnd, String newEventName) {
        String checkSQL = """
            SELECT 1 FROM bookings
            WHERE roomNum = ?
            AND timeStart < ?
            AND timeEnd > ?
            AND id != ?
            LIMIT 1;
            """;

        String updateSQL = """
            UPDATE bookings
            SET roomNum = ?, timeStart = ?, timeEnd = ?, eventName = ?
            WHERE id = ?;
            """;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            //Here we check for overlaps, similar to how we do it in creatbooking, but we also take note of the booking that's
            //being updated, so that we can ignore it. otherwise it will often overlap with itself.
            try (PreparedStatement stmt = conn.prepareStatement(checkSQL)) {
                stmt.setInt(1, newRoom);
                stmt.setString(2, newEnd.format(fmt));
                stmt.setString(3, newStart.format(fmt));
                stmt.setInt(4, oldBooking.getID());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    //If an overlap is found, we'll exit out and return false
                    return false;
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setInt(1, newRoom);
                updateStmt.setString(2, newStart.format(fmt));
                updateStmt.setString(3, newEnd.format(fmt));
                updateStmt.setString(4, newEventName);
                updateStmt.setInt(5, oldBooking.getID());

                int rowsAffected = updateStmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //This function allows use to delete bookings
    public boolean deleteBooking(booking bookingToDelete) {
        String deleteSQL = "DELETE FROM bookings WHERE id = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {

            stmt.setInt(1, bookingToDelete.getID());
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
