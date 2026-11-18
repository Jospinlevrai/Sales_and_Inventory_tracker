package dto;

/** A request that says which user is doing it. The server overwrites the id with the logged-in user's. */
public interface ActingUserRequest {
    void setUserId(int userId);
}
