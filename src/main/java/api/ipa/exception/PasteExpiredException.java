package api.ipa.exception;

public class PasteExpiredException extends RuntimeException{
    public PasteExpiredException(String message) {
        super(message);
    }
}
