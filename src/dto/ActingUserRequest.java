package dto;

/** A request that says which user is doing it. The server overwrites the id with the logged-in user's. */
import java.io.Serializable;

public interface ActingUserRequest {
    void setUserId(int userId);
}
    


