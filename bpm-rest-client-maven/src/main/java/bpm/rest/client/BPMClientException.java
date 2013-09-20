package bpm.rest.client;

public class BPMClientException extends Exception {

	private static final long serialVersionUID = -1629819458418243966L;
	private String errorDetails;
	private int statusCode;

	public BPMClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public BPMClientException(String message) {
		super(message);
	}

	public BPMClientException(String message, String errorDetails,
			int statusCode) {
		super(message);
		this.errorDetails = errorDetails;
		this.statusCode = statusCode;
	}

	public BPMClientException(Throwable cause) {
		super(cause);
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public int getStatusCode() {
		return statusCode;
	}

}
