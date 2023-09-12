package pk.ajneb97.model.internal;

public class PlayerKitsMessageResult {

    private String message;
    private boolean error;
    private boolean proceedToBuy;

    public PlayerKitsMessageResult(String message, boolean error) {
        this.message = message;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public static PlayerKitsMessageResult success(){
        return new PlayerKitsMessageResult(null,false);
    }

    public static PlayerKitsMessageResult error(String errorMessage){
        return new PlayerKitsMessageResult(errorMessage,true);
    }

    public boolean isProceedToBuy() {
        return proceedToBuy;
    }

    public void setProceedToBuy(boolean proceedToBuy) {
        this.proceedToBuy = proceedToBuy;
    }
}
