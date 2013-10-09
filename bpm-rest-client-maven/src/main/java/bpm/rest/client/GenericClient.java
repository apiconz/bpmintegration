package bpm.rest.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

import bpm.rest.client.authentication.AuthenticationTokenHandler;
import bpm.rest.client.authentication.AuthenticationTokenHandlerException;

public class GenericClient {

	private final static Logger LOGGER = Logger.getLogger(GenericClient.class
			.getName());

	private String hostname;
	private String protocol;
	private int port;
	private String uri;
	// private final String urlPattern;
	private boolean useSSL;
	private AuthenticationTokenHandler handler;
	private boolean reauthenticating = false;
	private int readTimeOut;
	private int connectionTimeOut;

	// private Client httpClient;

	/**
	 * Invoking this constructor is the same as invoking the primary constructor
	 * using false as the value for the useSSL boolean parameter.
	 */
	public GenericClient(String hostname, String uri, int port,
			AuthenticationTokenHandler handler, int readTimeOut,
			int connectionTimeOut) {
		this(hostname, uri, port, handler, true, readTimeOut, connectionTimeOut);
	}

	/**
	 * Primary constructor for this class. Note that before attempting to use an
	 * SSL connection with the other endpoint, the truststore [for the JVM where
	 * the client code is running] should have been properly configured.
	 * 
	 * @param hostname
	 *            - The hostname of the server to connect to.
	 * @param uri
	 *            - The uri of the resource.
	 * @param port
	 *            - The port number to connect to.
	 * @param handler
	 *            - The authentication token handler.
	 * @param useSSL
	 *            - States whether or not an HTTPS connection shall be
	 *            established.
	 */
	public GenericClient(String hostname, String uri, int port,
			AuthenticationTokenHandler handler, boolean useSSL,
			int readTimeOut, int connectionTimeOut) {
		super();
		this.hostname = hostname;
		this.port = port;
		this.uri = uri;
		this.useSSL = useSSL;
		protocol = (useSSL) ? "https" : "http";
		// this.urlPattern = protocol + "://" + hostname + ':' + port + uri
		// + "{0}";
		this.handler = handler;
		this.readTimeOut = readTimeOut;
		this.connectionTimeOut = connectionTimeOut;
		// this.httpClient = new Client(Protocol.HTTP);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("hostname: ");
		buffer.append(hostname);
		buffer.append(", port: ");
		buffer.append(port);
		buffer.append(", uri: ");
		buffer.append(uri);
		// buffer.append(", url pattern: ");
		// buffer.append(urlPattern);
		return buffer.toString();
	}

	public JSONObject executeRESTCall(String relativePath,
			Map<String, Object> arguments, Method method,
			boolean putContentInBody) throws BPMClientException,
			AuthenticationTokenHandlerException {
		System.out.println("--->executeRESTCall");
		// Prepare the request object
		String url;
		Request request;
		if (putContentInBody) {
			url = buildURL(relativePath, null);
			String requestBody = encodeArguments(arguments, true);
			System.out.println("HTTP call: " + url);
			System.out.println("Request body: " + requestBody);
			LOGGER.log(Level.INFO, "HTTP call: " + url);
			LOGGER.log(Level.INFO, "Request body: " + requestBody);
			StringRepresentation sp = new StringRepresentation(requestBody);
			sp.setMediaType(MediaType.APPLICATION_WWW_FORM);
			request = new Request(method, url, sp);
		} else {
			url = buildURL(relativePath, arguments);
			System.out.println("HTTP call: " + url);
			LOGGER.log(Level.INFO, "HTTP call: " + url);
			request = new Request(method, url);
			// The following is to avoid 411 error on certain servers that
			// require a content-length to be specified
			request.setEntity("\n", MediaType.TEXT_PLAIN);
		}

		if (handler.foundAuthenticationToken()) {
			// Add authentication token to request
			handler.addAuthenticationToken(request);
		} else if (!handler.isUsingUserIdentityInContainer()) {
			// Add userid and password to request only if not using user
			// identity in the container
			addAuthenticationChallengeResponse(request);
		}

		// Indicates the client preferences and let the server handle the best
		// representation with content negotiation.
		request.getClientInfo().getAcceptedMediaTypes()
				.add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

		Client httpClient = null;
		try {
			Protocol protocol = (useSSL) ? Protocol.HTTPS : Protocol.HTTP;
			Context ctx = new Context();
			Series<Parameter> pa = ctx.getParameters();
			pa.add(new Parameter("readTimeout", Integer.toString(readTimeOut)));
			pa.add(new Parameter("socketConnectTimeoutMs", Integer
					.toString(connectionTimeOut)));
			httpClient = new Client(ctx, protocol);
			// Ask the HTTP client connector to handle the call
			Response response = httpClient.handle(request);
			return processResponse(response);
		} catch (BPMClientException lcException) {
			if ((lcException.getStatusCode() == 401 || lcException
					.getStatusCode() == 403)
					&& handler.foundAuthenticationToken() && !reauthenticating) {
				handler.reset();
				reauthenticating = true;
				try {
					System.out.println("Reauthenticating...");
					LOGGER.log(Level.WARNING, "Reauthenticating...");
					return executeRESTCall(relativePath, arguments, method,
							putContentInBody);
				} finally {
					reauthenticating = false;
					System.out.println("Reauthenticating attempt completed...");
					LOGGER.log(Level.WARNING,
							"Reauthenticating attempt completed...");
				}
			}
			throw (lcException);
		} finally {
			try {
				if (httpClient != null)
					System.out.println("va a parar el httpClient");
					httpClient.stop();
			} catch (Exception e) {
				// throw new BPMClientException(e.getMessage());
				
				System.out.println("paso por excepcion que no hace nada");
			}
		}
	}

	private void addAuthenticationChallengeResponse(Request request) {
		// Add the client authentication to the request
		ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
		ChallengeResponse authentication = new ChallengeResponse(scheme,
				handler.getUserid(), handler.getPassword());
		request.setChallengeResponse(authentication);
	}

	private JSONObject processResponse(Response response)
			throws BPMClientException, AuthenticationTokenHandlerException {
		try {
			if (response.getStatus().isSuccess()) {
				if (!handler.foundAuthenticationToken()
						&& !handler.isUsingUserIdentityInContainer()) {
					handler.readAuthenticationToken(response);
				}
				LOGGER.log(Level.INFO,
						"HTTP request was successfully processed by the server.");
				logHandler();
				return getJSONObject(response);
			} else if (response.getStatus().equals(
					Status.CLIENT_ERROR_FORBIDDEN)
					|| response.getStatus().equals(
							Status.CLIENT_ERROR_UNAUTHORIZED)) {
				// Unauthorized access
				LOGGER.log(Level.INFO, "Could not authenticate user!");
				logHandler();
				throw new BPMClientException(
						"The server could not authenticate the user or the user does not have enough rights to access the requested resource.",
						response.getStatus().toString(), response.getStatus()
								.getCode());
			} else {
				logResponseData(response);
				// Unexpected status
				JSONObject jsonObject = getJSONObject(response);
				throw new BPMClientException(jsonObject.getJSONObject("Data")
						.getString("errorMessage"));
			}
		} catch (JSONException je) {
			throw new BPMClientException(je);
		}
	}

	private JSONObject getJSONObject(Response response)
			throws BPMClientException {
		try {
			if (isResponseJson(response)) {
				Representation rep = response.getEntity();
				rep.setCharacterSet(CharacterSet.UTF_8);
				JsonRepresentation jsonRep = new JsonRepresentation(rep);
				JSONObject jsonObj = jsonRep.getJsonObject();
				return jsonObj;
			} else {
				String respEntity = response.getEntityAsText();
				LOGGER.log(Level.SEVERE, "Non-JSON response entity\n---\n"
						+ respEntity + "\n---");
				throw new BPMClientException(
						"An error occurred (and server did not return JSON). Additional error information follows: "
								+ response, response.getStatus().toString(),
						response.getStatus().getCode());
			}
		} catch (IOException ioe) {
			throw new BPMClientException(ioe);
		} catch (JSONException je) {
			throw new BPMClientException(je);
		}
	}

	private boolean isResponseJson(Response response) {
		Map<String, Object> attributes = response.getAttributes();
		@SuppressWarnings("unchecked")
		Series<Header> header = (Series<Header>) attributes
				.get(HeaderConstants.ATTRIBUTE_HEADERS);
		// Form form = (Form) attributes.get("org.restlet.http.headers");
		if (header != null) {
			String contentType = header.getValues("Content-Type");
			if (contentType != null) {
				return (contentType.contains("application/json"));
			}
		}
		return false;
	}

	private String buildURL(String relativePath, Map<String, Object> arguments)
			throws BPMClientException {
		try {
			URI uriObj = new URI(protocol, null, hostname, port, uri
					+ relativePath, null, null);
			String url = uriObj.toASCIIString();
			String query = encodeArguments(arguments, true);
			return appendQuery(url, query);
		} catch (URISyntaxException e) {
			throw new BPMClientException(e);
		}
	}

	private String appendQuery(String path, String query) {
		return (query != null && query.length() > 0) ? (path + '?' + query)
				: path;
	}

	private String encodeArguments(Map<String, Object> arguments,
			boolean includeNullValues) throws BPMClientException {

		if (arguments == null) {
			// return "";
			return null;
		}
		try {
			Form form = new Form();
			Iterator<String> keys = arguments.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Object objValue = arguments.get(key);
				if (!includeNullValues) {
					if (objValue == null) {
						continue;
					}
				}

				if (objValue == null) {
					form.add(key, valueToString(null));
					continue;
				}

				Object[] values = (objValue.getClass().isArray()) ? (Object[]) objValue
						: new Object[] { objValue };

				LOGGER.log(Level.INFO, "Converting values to String!");

				for (Object value : values) {
					String strValue;
					if (value instanceof JSONObject) {
						LOGGER.log(Level.FINE,
								"Converting a JSONObject to String!");
						strValue = valueToString(value);
					} else {
						LOGGER.log(Level.FINE,
								"Converting a non-JSONObject to String!");
						strValue = value.toString();
					}

					form.add(key, strValue);
				}
			}
			return form.getQueryString();

		} catch (JSONException e) {
			throw new BPMClientException(e);
		}
	}

	private String valueToString(Object value) throws JSONException {
		if (value == null || value.equals(null)) {
			return "null";
		}
		if (value instanceof JSONString) {
			Object object;
			try {
				object = ((JSONString) value).toJSONString();
			} catch (Exception e) {
				throw new JSONException(e);
			}
			if (object instanceof String) {
				return (String) object;
			}
			throw new JSONException("Bad value from toJSONString: " + object);
		}
		if (value instanceof Number) {
			return JSONObject.numberToString((Number) value);
		}
		if (value instanceof Boolean || value instanceof JSONObject
				|| value instanceof JSONArray) {
			return value.toString();
		}
		if (value instanceof Map) {
			return new JSONObject((Map) value).toString();
		}
		if (value instanceof Collection) {
			return new JSONArray((Collection) value).toString();
		}
		if (value.getClass().isArray()) {
			return new JSONArray(value).toString();
		}
		return JSONObject.quote(value.toString());
	}

	private void logHandler() {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, handler.toString());
		}
	}

	private void logResponseData(Response response) {

		StringBuffer sb = new StringBuffer();

		@SuppressWarnings("unchecked")
		Series<Header> hSeries = (Series<Header>) response.getAttributes().get(
				HeaderConstants.ATTRIBUTE_HEADERS);

		if (hSeries != null) {
			Set<String> hNames = hSeries.getNames();
			if (hNames != null) {
				sb.append("\nResponse Headers:\n");
				for (String hName : hNames) {
					Header thisHeader = hSeries.getFirst(hName);
					sb.append("   name=[" + thisHeader.getName() + "] value=["
							+ thisHeader.getValue() + "]\n");
				}
				sb.append("End Response headers");
			}
		}

		// This breaks the code
		// String responseEntity = response.getEntityAsText();
		// if (responseEntity != null) {
		// sb.append("\nResponse entity\n");
		// sb.append(responseEntity);
		// sb.append("End Response entity");
		// }

		sb.append("\nResponse status: ");
		sb.append(response.getStatus().getDescription());

		sb.append("\nResponse status code: ");
		sb.append(response.getStatus().getCode());

		sb.append("\nResponse phrase: ");
		sb.append(response.getStatus().getReasonPhrase());

		sb.append("\nResponse: ");
		sb.append(response.getStatus().toString());

		if (sb.length() > 0) {
			LOGGER.log(Level.SEVERE, "HTTP response: " + sb.toString());
		}

		if (response.getStatus().getThrowable() != null) {
			Throwable error = response.getStatus().getThrowable();
			StringBuffer exceptionHeader = new StringBuffer();
			exceptionHeader.append("\nException info:\n");
			exceptionHeader.append("Exception message: ");
			exceptionHeader.append(error.getMessage());
			LOGGER.log(Level.SEVERE, exceptionHeader.toString());
			do {
				LOGGER.log(Level.SEVERE, "Exception stack trace: ", error);
				error = error.getCause();
			} while (error != null);
			LOGGER.log(Level.SEVERE, "\nEnd Exception info");
		}
	}
}
