/** Unit 3: custom checked exception for bad or unregistered mobile numbers. */
public class InvalidNumberException extends Exception {
    public InvalidNumberException(String message) { super(message); }
}
