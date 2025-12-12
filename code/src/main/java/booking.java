import java.awt.*;
import java.time.LocalDateTime;

public class booking {
    public int roomID;
    public LocalDateTime timeStart;
    public LocalDateTime timeEnd;
    public String eventName;
    public String authorName;
    public int bookingID;
    public Color bookingColor = Color.green;

    public booking(int roomID, LocalDateTime timeStart, LocalDateTime timeEnd, String eventName, String authorName, int bookingID){
        this.roomID = roomID;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.eventName = eventName;
        this.authorName = authorName;
        this.bookingID = bookingID;
    }

    //These methods were added so that we could add different colors to the schedule view, but we never got it working.
    //The schedule view still uses these methods to pull the color of a given booking, but they always default to green.
    public void setBookingColor(Color newColor){
        this.bookingColor = newColor;
    }

    public Color getBookingColor(){
        return this.bookingColor;
    }

    //The rest of these are just getters for various methods we want to pull throughout the project
    public int getRoomNum() {
        return roomID;
    }

    public LocalDateTime getStartTime() {
        return timeStart;
    }

    public LocalDateTime getEndTime() {
        return timeEnd;
    }

    public String getEventName() {
        return eventName;
    }

    public String getAuthor() {
        return authorName;
    }

    public int getID(){
        return bookingID;
    }
}
