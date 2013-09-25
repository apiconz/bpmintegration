package bpm.rest.client.authentication.was;

import java.security.GeneralSecurityException;
import java.util.Set;

import javax.security.auth.Subject;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.util.Series;

import bpm.rest.client.authentication.AuthenticationTokenHandler;
import bpm.rest.client.authentication.AuthenticationTokenHandlerException;

import com.ibm.websphere.security.auth.WSSubject;
import com.ibm.websphere.security.cred.WSCredential;
    
/**
 * An instance of this class is used to handle user authentication between a
 * client program and a WebSphere Application Server (the Lombardi Edition
 * Process Center/Server runs on a WebSphere Application Server).
 * 
 * The client code can be part of a standalone Java program or it can be part of
 * a JEE application that runs on a WebSphere Application Server.
 * 
 * If instead of developing a Java standalone program, you are developing code
 * to be deployed on the WebSphere Application Server and single sign-on was
 * configured for Application Server and Lombardi Edition server, then the
 * no-argument constructor of the WASAuthenticationTokenHandler class shall be
 * used. Doing so ensures that the LTPA token value is read and included in the
 * request object sent to the Lombardi Edition server (thus, requiring no user
 * and password). In other words, under such cases, the method calls are
 * automatically made under the identity of the authenticated user who is
 * accessing the web application (of course, it is assumed that the JEE
 * application is secured).
 * 
 * If the client is a stand alone program or if the Application Server where the
 * client is running does not have SSO enabled, then the authentication
 * credentails for the user (i.e., userid & password) to connect to the Lombardi
 * Edition server shall be passed at instantiation time.
 * 
 * This class assumes that 'LtpaToken2' is the name for the LTPA token cookie.
 * If a different name shall be used to reference the cookie that holds the LTPA
 * token value, then that name shall be passed in at instantiation time.
 * 
 * This class has been successfully tested and used on WAS 7.x.
 * 
 */
public class WASAuthenticationTokenHandler implements
		AuthenticationTokenHandler {

	private String userid;
	private String password;
	private CookieSetting ltpaToken;
	private String cookieName;
	private boolean usingUserIdentityInContainer;

	/**
	 * Use this constructor if the client code is running on a WebSphere
	 * Application Sever and both, WAS and Lombardi, were configured to support
	 * SSO.
	 * 
	 * @throws AuthenticationTokenHandlerException
	 */
	public WASAuthenticationTokenHandler()
			throws AuthenticationTokenHandlerException {
		init(null, null, "LtpaToken2");
	}

	/**
	 * Use this constructor if the client is running in a Java standalone
	 * program or if the WAS server [where the client code is running] and the
	 * Lombardi Edition server do not have SSO enabled.
	 * 
	 * @param userid
	 * @param password
	 * @throws AuthenticationTokenHandlerException
	 */
	public WASAuthenticationTokenHandler(String userid, String password)
			throws AuthenticationTokenHandlerException {
		init(userid, password, "LtpaToken2");
	}

	public WASAuthenticationTokenHandler(String cookieName)
			throws AuthenticationTokenHandlerException {
		init(null, null, cookieName);
	}

	public WASAuthenticationTokenHandler(String userid, String password,
			String cookieName) throws AuthenticationTokenHandlerException {
		init(userid, password, cookieName);
	}

	private void init(String userid, String password, String cookieName)
			throws AuthenticationTokenHandlerException {
		this.cookieName = cookieName;
		this.userid = userid;
		this.password = password;
		if (userid == null || password == null) {
			this.usingUserIdentityInContainer = true;
			initLtpaToken();
		}
	}

	public void readAuthenticationToken(Response response)
			throws AuthenticationTokenHandlerException {
		if (usingUserIdentityInContainer) {
			throw new AuthenticationTokenHandlerException(
					"This method shall only be called when not using user identity from container.");
		} else {
			Series<CookieSetting> cookies = response.getCookieSettings();
			CookieSetting cookie = cookies.getFirst(cookieName);
			// cookie.setSecure(true);
			this.ltpaToken = cookie;
		}
	}

	public void addAuthenticationToken(Request request)
			throws AuthenticationTokenHandlerException {
		if (ltpaToken != null) {
			Series<Cookie> cookies = request.getCookies();
			cookies.add(ltpaToken);
		}
	}

	public boolean foundAuthenticationToken() {
		return (ltpaToken != null);
	}

	public boolean isUsingUserIdentityInContainer() {
		return usingUserIdentityInContainer;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("-- WAS authentication handler (start) --");
		buffer.append('\n');
		if (usingUserIdentityInContainer) {
			buffer.append("LTPA token value: ");
			buffer.append(ltpaToken.getValue());
			buffer.append('\n');
		}
		buffer.append("Cookie name: ");
		buffer.append(cookieName);
		buffer.append('\n');
		buffer.append("Using user identity in container: ");
		buffer.append(usingUserIdentityInContainer);
		buffer.append('\n');
		buffer.append("userid: ");
		buffer.append(userid);
		buffer.append('\n');
		buffer.append("-- WAS authentication handler (end) --");
		return buffer.toString();
	}

	public String getUserid() {
		return userid;
	}

	public String getPassword() {
		return password;
	}

	public void reset() throws AuthenticationTokenHandlerException {
		ltpaToken = null;
		if (usingUserIdentityInContainer) {
			initLtpaToken();
		}
	}

	private WSCredential readWSCredential()
			throws AuthenticationTokenHandlerException {
		try {
			WSCredential credential = null;
			// Get current security subject
			Subject subject = WSSubject.getRunAsSubject();
			if (subject != null) {
				@SuppressWarnings("rawtypes")
				Set credentials = subject
						.getPublicCredentials(WSCredential.class);
				if (credentials.size() > 0) {
					credential = (WSCredential) credentials.iterator().next();
				}
			}
			return credential;
		} catch (GeneralSecurityException e) {
			throw new AuthenticationTokenHandlerException(e);
		}
	}

	private void initLtpaToken() throws AuthenticationTokenHandlerException {
		WSCredential credential = readWSCredential();
		try {
			if (credential != null) {
				String user = (String) credential.getSecurityName();
				System.out.println("@@@user=" + user);
				if (!user.equalsIgnoreCase("UNAUTHENTICATED")) {
					byte[] token = credential.getCredentialToken();
					if (token != null) {
						String ltpaValue = com.ibm.ws.webservices.engine.encoding.Base64
								.encode(token);
						ltpaToken = new CookieSetting(cookieName, ltpaValue);
						//ltpaToken.setSecure(true);
					}
				}
			}
		} catch (GeneralSecurityException e) {
			throw new AuthenticationTokenHandlerException(e);
		}

		if (ltpaToken == null) {
			throw new AuthenticationTokenHandlerException(
					"Could not read LTPA token value from container.");
		}
	}

}
