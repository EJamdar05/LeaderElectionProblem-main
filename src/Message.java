import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    public UUID msgId;
    public int flag;

    public Message(){
        UUID uuid = new UUID(32,64);
        this.msgId = UUID.randomUUID();
        this.flag = 0;
    }

    public String toString(){
        return this.msgId +": Flag = "+this.flag;
    }
}
