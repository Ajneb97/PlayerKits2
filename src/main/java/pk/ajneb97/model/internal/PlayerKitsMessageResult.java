package pk.ajneb97.model.internal;

public class PlayerKitsMessageResult {

    private final String message;
    private final boolean error;
    private boolean proceedToBuy;

    public PlayerKitsMessageResult(String message, boolean error) {
        this.message = message;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
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
