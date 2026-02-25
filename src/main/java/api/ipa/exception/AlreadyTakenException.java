package api.ipa.exception;

public class AlreadyTakenException extends RuntimeException{
    public AlreadyTakenException(String message){
        super(message);
    }
}
