import java.sql.*;
import java.io.File;
import java.net.URISyntaxException;

public class LoginHandler {
    //BookKeeper will use SQLite instead of mySQL. This is to keep the project portable.
    /*Full disclosure: AI systems, namely GPT, helped a lot in explaining how to implement SQLite systems in Java. Nothing was copy/pasted.
    Originally I wanted to use a simpler system, as implementing sql into java seemed a bit daunting due to additional requirement downloads,
    but I knew I wanted to use SQL for the schedule database later on, so I settled on learning how to use SQLite as a compromise.
     */

        //This string specifies the file that our database will be stored in.
        private static String DB_URL;

        //This function is used to initialize a database in the specified url (our login.db)
        public static void initialize(){

            File jarFile = getJarDirectory(Main.class);
            File jarDir = jarFile.getParentFile();

            File dbFile = new File(jarDir, "login.db");
            DB_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            System.out.println(DB_URL);


            //Try and catch is used to deal with any exceptions thrown by our SQL integration.
            //The try block also stops the connection to the database (conn) once we've finished using it.
            try (Connection conn = DriverManager.getConnection(DB_URL)){
                //Our connection to the database uses a statement to send commands to the SQL server.
                Statement stmt = conn.createStatement();

                //By creating new strings for commands, then feeding them to conn through our statement, we can send them to the database to be processed.
                String sql = """
                        CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username VARCHAR(20) UNIQUE NOT NULL,
                        password VARCHAR(20) NOT NULL,
                        account_type TEXT NOT NULL
                        );
                        """;
                stmt.execute(sql);

                //TEST VALUES
                String insert = """
                        INSERT OR IGNORE INTO users(username, password, account_type)
                        VALUES
                        ('student1', 'studentpass', 'Student'),
                        ('teacher1', 'teacherpass', 'Teacher'),
                        ('admin1', 'adminpass', 'Admin');
                        """;
                //stmt.execute(insert);

                //Print this to indicate no exceptions have been thrown.
                System.out.println("Login Database initialized without error.");

            //We'll use the catch to handle sql exceptions thrown by conn
            } catch(SQLException e){
                e.printStackTrace();

            }
        }

        /*
        This is another thing that AI helped me figure out. I wanted to be able to run the program as a single jar file
        but that introduced a host of complications regarding creating and populating new database files at runtime.
        This function is used solely to tell where the jar file is located on the system that is currently running it so that
        it can create and use database files in that directory instead of C:User/ which is where it runs by default and also lacks
        write access, so it isn't able to initialize the database. It's got AI fingerprints throughout it, but it's a small unnecessary feature
        that I wanted to add as a learning experience, so I don't mind. This has no real effect when running from the IDE other than moving where login.db is located,
        so in that sense it's completely superfluous and entirely unneeded.
         */
        public static File getJarDirectory(Class<?> mainClass){
            try{
                File jarFile = new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI());
                return jarFile;
            }catch(Exception e){
                throw new RuntimeException("Unable to determine JAR directory", e);
            }
        }

        //This function originally returned a boolean, but I wanted more definition when outputting reasons for creation failure, so created a new class.
        public UserCreationResult createUser(String username, String password, String accountType){
            //This function works the same way as our authentication check, but inserts instead of retrieving.
            String sql = "INSERT INTO users(username, password, account_type) VALUES (?, ?, ?)";

            try(Connection conn = DriverManager.getConnection(DB_URL)){
                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, accountType);

                int rowsAffected = stmt.executeUpdate();
                return new UserCreationResult(rowsAffected > 0, "User Creation Successful!");

            }catch(SQLException e){
                e.printStackTrace();
                return new UserCreationResult(false, e.getMessage());
            }
        }

        //This function will authenticate a given username and password combination as either being correct (true) or incorrect (false).
        public boolean authenticate(String username, String password){
            //We'll set up our Query here, leaving a ? to be filled in later
            String sql = "SELECT password FROM users WHERE username = ?";
            //Just like our initialize function, we'll use a try and catch to open and close our connection and catch sql exceptions.
            try (Connection conn = DriverManager.getConnection(DB_URL)){

                /*Using a prepare statement like this instead of feeding the username field directly into the query helps to avoid user meddling.
                For example, if the user input their username as 'anything' they could modify the query. PrepareStatement helps to avoid this.
                This was a suggestion made by AI that I had no prior knowledge of, but thought it would be a good addition to our login system's security after learning why it was suggested.
                 */
                PreparedStatement stmt = conn.prepareStatement(sql);

                //We can then replace our ? field in the sql query string with the entered username.
                stmt.setString(1, username);
                //We can take the return of our query to check if the passwords match
                ResultSet rs = stmt.executeQuery();

                //rs.next() will check the first row of our query result. if that first row doesn't exist, we won't check the password, as none was returned.
                if(rs.next()){
                    //Once we know that a row has been returned, we can use rs.getString to return the string value of the specified attribute / column. in this case the "password" attribute.
                    String storedPassword = rs.getString("password");
                    //finally, if this password matches the password provided, we can return true to let the calling function know the user authenticated correctly
                    return password.equals(storedPassword);
                }
            } catch(SQLException e){
                e.printStackTrace();
            }
            return false;
        }

        //these next two functions are very similar and were pretty much just copy pastes of the original authentication function before I learned about implementing PreparedStatement
        //One returns an int of the user's unique id, the other a string of the user's account type.
        public int getUserID(String username){
            String sql = "SELECT id FROM users WHERE username = '" + username + "'";
            try (Connection conn = DriverManager.getConnection(DB_URL)){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if(rs.next()){
                    return rs.getInt("id");
                }


            } catch(SQLException e){
                e.printStackTrace();
            }
            return 0;
        }

        //GetUserName and GetUserType were added later as we found that functionality rather useful to have
        public String getUserName(int userID){
            String sql = "SELECT username FROM users WHERE id = '" + userID + "'";
            try (Connection conn = DriverManager.getConnection(DB_URL)){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if(rs.next()){
                    return rs.getString("username");
                }


            } catch(SQLException e){
                e.printStackTrace();
            }
            //We should not be able to get here
            return null;
        }

        public String getUserType(int userID){
            String sql = "SELECT account_type FROM users WHERE id = " + userID;
            try (Connection conn = DriverManager.getConnection(DB_URL)){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if(rs.next()){
                    return rs.getString("account_type");
                }


            } catch(SQLException e){
                e.printStackTrace();
            }
            //If there are any issues returning the user's account type, we return student for safety. This should never happen though.
            return "Student";
        }

        //Similar to our original create user function, this one returns a boolean to notify the user of success.
    // This could probably be changed to provide a new object like userCreationResult, but it fell outside of our time budget.
    public boolean updateUser(int userID, String newUsername, String newPassword) {
        String sql;

        //This is the other half of our account management tab.
        //If the password hasn't been updated by the user the save button will pass the new password down as null. we can detect that here to
        //avoid updating the user's password.
        if (newPassword == null) {
            sql = "UPDATE users SET username = ? WHERE id = ?";
        } else {
            sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            if (newPassword == null) {
                stmt.setString(1, newUsername);
                stmt.setInt(2, userID);
            } else {
                stmt.setString(1, newUsername);
                stmt.setString(2, newPassword);
                stmt.setInt(3, userID);
            }

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



}


