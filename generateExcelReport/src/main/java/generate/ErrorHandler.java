package generate;

public class ErrorHandler extends RuntimeException {
	ErrorHandler(String message) {
		super(message);
	}
}
