import java.time.LocalDateTime;

public class booking {
    int roomID;
    LocalDateTime timeStart;
    LocalDateTime timeEnd;
    String eventName;
    String authorName;

    public booking(int roomID, LocalDateTime timeStart, LocalDateTime timeEnd, String eventName, String authorName){
        this.roomID = roomID;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.eventName = eventName;
        this.authorName = authorName;
    }
}
