package bpm.rest.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.restlet.data.Method;

import bpm.rest.client.authentication.AuthenticationTokenHandler;
import bpm.rest.client.authentication.AuthenticationTokenHandlerException;

public class BPMClientImpl implements BPMClient {

	private static final String propsFile = "bpm-client.properties";
	private String columns[] = { "instanceId", "bpdName", "instanceStatus",
			"instanceProcessApp", "assignedToUser", "assignedToRole",
			"taskStatus", "taskDueDate", "taskPriority", "taskReceivedDate",
			"taskActivityName","department", "hiringManager" };
	private GenericClient client;

	/**
	 * Note that before attempting to use an SSL connection with the BPM server,
	 * the truststore [for the JVM where the client code is running] should have
	 * been properly configured. Otherwise, errors will occur when attempting to
	 * use SSL. This method uses the URI value specified in the
	 * bpm-client.properties file. If no value can be found for the URI property
	 * in the bpm-client.properties file, then the default value
	 * "/rest/bpm/wle/v1" is used.
	 * 
	 * @param hostname
	 *            - The hostname where the BPM server is installed.
	 * @param port
	 *            - The port number for the BPM server.
	 * @param handler
	 *            - The authentication token handler.
	 * @param useSSL
	 *            - States whether or not an HTTPS connection shall be
	 *            established.
	 * @throws BPMClientException
	 */
	public BPMClientImpl(String hostname, int port,
			AuthenticationTokenHandler handler, boolean useSSL)
			throws BPMClientException {
		this(hostname, port, handler, useSSL, null);
	}

	/**
	 * Note that before attempting to use an SSL connection with the BPM server,
	 * the truststore [for the JVM where the client code is running] should have
	 * been properly configured. Otherwise, errors will occur when attempting to
	 * use SSL.
	 * 
	 * @param hostname
	 *            - The hostname where the BPM server is installed.
	 * @param port
	 *            - The port number for the BPM server.
	 * @param handler
	 *            - The authentication token handler.
	 * @param useSSL
	 *            - States whether or not an HTTPS connection shall be
	 *            established.
	 * @param uri
	 *            - The REST URI (this becomes part of the URL when invoking a
	 *            REST call). If a value is specified for this parameter, then
	 *            the value specified in the bpm-client.properties file is
	 *            ignored. If no value can be found for this property in the
	 *            bpm-client.properties file, then the default value
	 *            "/rest/bpm/wle/v1" is used.
	 * @throws BPMClientException
	 */
	public BPMClientImpl(String hostname, int port,
			AuthenticationTokenHandler handler, boolean useSSL, String uri)
			throws BPMClientException {
		super();
		try {
			Properties props = new Properties();
			props.load(this.getClass().getResourceAsStream(propsFile));
			

			System.out.println("hito!!!!!!");
			
			String URI = (uri != null) ? uri : props.getProperty("uri",
					"/rest/bpm/wle/v1");
			int readTimeOut = Integer.valueOf(props.getProperty("readTimeOut",
					"300000"));
			int connectionTimeOut = Integer.valueOf(props.getProperty(
					"connectionTimeOut", "8000"));
			this.client = new GenericClient(hostname, URI, port, handler,
					useSSL, readTimeOut, connectionTimeOut);
		} catch (IOException e) {
			throw new BPMClientException(e.getMessage());
		}
	}

	/**
	 * Invokes the primary constructor using a value of false for the useSSL
	 * boolean parameter.
	 * 
	 * @param hostname
	 * @param port
	 * @param handler
	 * @throws BPMClientException
	 */
	public BPMClientImpl(String hostname, int port,
			AuthenticationTokenHandler handler) throws BPMClientException {
		this(hostname, port, handler, false);
	}

	@Override
	public String toString() {
		return client.toString();
	}

	public JSONObject executeRESTCall(String relativePath,
			Map<String, Object> arguments, Method method,
			boolean putContentInBody) throws BPMClientException,
			AuthenticationTokenHandlerException {
		return client.executeRESTCall(relativePath, arguments, method,
				putContentInBody);
	}

	public JSONObject runService(String serviceName, JSONObject arguments)
			throws BPMClientException, AuthenticationTokenHandlerException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("action", "start");
		if (arguments != null) {
			args.put("params", arguments);
		}
		return client.executeRESTCall("/service/" + serviceName, args,
				Method.POST, true);
	}

	public JSONObject resumeService(int serviceId, String resumeKey)
			throws BPMClientException, AuthenticationTokenHandlerException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("action", resumeKey);
		args.put("parts", "all");
		return client.executeRESTCall("/service/" + serviceId, args,
				Method.POST, false);
	}

	public JSONObject setData(int serviceId, JSONObject arguments)
			throws BPMClientException, AuthenticationTokenHandlerException {
		Map<String, Object> args = new HashMap<String, Object>();
		if (arguments != null && arguments.length() > 0) {
			args.put("action", "setData");
			args.put("params", arguments);
			return client.executeRESTCall("/service/" + serviceId, args,
					Method.POST, true);
		} else {
			throw new BPMClientException(
					"A populated JSONObject arguments parameter must be provided to this method.");
		}
	}

	public JSONObject getData(int serviceId, String[] fields)
			throws BPMClientException, AuthenticationTokenHandlerException {
		Map<String, Object> args = new HashMap<String, Object>();
		if (fields != null && fields.length > 0) {
			args.put("action", "getData");
			args.put("fields", StringUtils.join(fields, ','));
			return client.executeRESTCall("/service/" + serviceId, args,
					Method.GET, false);
		} else {
			throw new BPMClientException(
					"One or more field names must be provided to this method.");
		}
	}

	
	public JSONObject startTask(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("action", "start");
		return client.executeRESTCall("/task/" + taskId, args, Method.POST,
				false);
	}


	public JSONObject finishTask(int taskId, JSONObject arguments)
			throws BPMClientException, AuthenticationTokenHandlerException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("action", "finish");
		if (arguments != null) {
			args.put("params", arguments);
		}
		args.put("parts", "all");
		return client.executeRESTCall("/task/" + taskId, args, Method.POST,
				false);
	}

	
	public JSONObject getExternalActivityModel(String externalActivityId)
			throws BPMClientException, AuthenticationTokenHandlerException {
		return client.executeRESTCall("/externalactivity/" + externalActivityId
				+ "/model", null, Method.GET, false);
	}

	public JSONObject getTaskDetails(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException {
		return client.executeRESTCall("/task/" + taskId, null, Method.GET,
				false);
	}

	
	public JSONObject search(String columns[], String conditions[],
			String organization, String firstColumnSort, String secondColumnSort)
			throws BPMClientException, AuthenticationTokenHandlerException {

		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("columns", StringUtils.join(columns, ','));
		arguments.put("condition", conditions);
		arguments.put("sort", firstColumnSort);
		arguments.put("secondSort", secondColumnSort);
		arguments.put("organization", organization);
		// String args = encodeArguments(arguments, false);
		// String relativePath = appendArguments("/search/query", args);
		return client.executeRESTCall("/search/query", arguments, Method.PUT,
				false);
	}

	
	public JSONObject runBPD(String bpdId, String processAppId,
			JSONObject arguments) throws BPMClientException,
			AuthenticationTokenHandlerException {

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("action", "start");
		args.put("bpdId", bpdId);
		args.put("processAppId", processAppId);
		if (arguments != null) {
			args.put("params", arguments);
		}
		return client.executeRESTCall("/process", args, Method.POST, true);
	}

	
	public JSONObject getBPDInstanceDetails(int instanceId)
			throws BPMClientException, AuthenticationTokenHandlerException {

		// String relativePath = "/process/" + instanceId;
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("parts", "all");
		return client.executeRESTCall("/process/" + instanceId, arguments,
				Method.GET, false);
	}

	
	public JSONObject assignTask(int taskId, String userid)
			throws BPMClientException, AuthenticationTokenHandlerException {

		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("action", "assign");
		arguments.put("toUser", userid);
		return client.executeRESTCall("/task/" + taskId, arguments,
				Method.POST, false);
	}

	
	public JSONObject assignTask(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException {

		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("action", "assign");
		arguments.put("toMe", true);
		return client.executeRESTCall("/task/" + taskId, arguments,
				Method.POST, false);
	}

	
	public JSONObject returnTask(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("action", "assign");
		arguments.put("back", "true");
		return client.executeRESTCall("/task/" + taskId, arguments,
				Method.POST, false);
	}

	
	public JSONObject getInbox() throws BPMClientException,
			AuthenticationTokenHandlerException {
		String conditions[] = { "taskStatus|New_or_Received" };
		return search(columns, conditions, "byInstance", "instanceId", "taskId");

		// relativePath = "/search/meta/businessData";
		// return client.executeRESTCall(relativePath, null, Method.GET);
	}

	
	public JSONObject getTasks(String bpdName) throws BPMClientException,
			AuthenticationTokenHandlerException {
		String conditions[] = { "taskStatus|New_or_Received",
				"bpdName|" + bpdName };

		return search(columns, conditions, "byInstance", "instanceId", "taskId");
	}

	
	public void executeJS(int processId, String js) throws BPMClientException,
			AuthenticationTokenHandlerException {

		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("action", "js");
		arguments.put("script", js);
		client.executeRESTCall("/process/" + processId, arguments, Method.PUT,
				false);
	}

}
