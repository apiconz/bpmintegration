package bpm.rest.client.authentication;

public class AuthenticationTokenHandlerException extends Exception {

	private static final long serialVersionUID = -882416004744683641L;

	public AuthenticationTokenHandlerException(String message) {
		super(message);
	}
 
	public AuthenticationTokenHandlerException(Throwable cause) {
		super(cause);
	}

}
