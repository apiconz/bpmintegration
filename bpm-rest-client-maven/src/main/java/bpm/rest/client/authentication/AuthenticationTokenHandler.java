package bpm.rest.client.authentication;

import org.restlet.Request;
import org.restlet.Response;

public interface AuthenticationTokenHandler {
 
	public void readAuthenticationToken(Response response)
			throws AuthenticationTokenHandlerException;

	public void addAuthenticationToken(Request request)
			throws AuthenticationTokenHandlerException;

	public boolean foundAuthenticationToken();

	public boolean isUsingUserIdentityInContainer();

	public String getUserid();

	public String getPassword();
 
	public void reset() throws AuthenticationTokenHandlerException;
}
