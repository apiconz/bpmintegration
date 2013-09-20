package bpm.rest.client;

import org.json.JSONObject;

import bpm.rest.client.authentication.AuthenticationTokenHandlerException;

public interface BPMClient {

	/**
	 * Starts a business process in WebSphere Lombardi Edition.
	 * 
	 * @param bpdId
	 * @param processAppId
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject runBPD(String bpdId, String processAppId,
			JSONObject arguments) throws BPMClientException,
			AuthenticationTokenHandlerException;

	/**
	 * Gets details about a business process instance.
	 * 
	 * @param instanceId
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject getBPDInstanceDetails(int instanceId)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Executes/completes a task (i.e., activity) in a business process.
	 * 
	 * @param taskId
	 * @param arguments
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject finishTask(int taskId, JSONObject arguments)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Gets the details for a task/activity.
	 * 
	 * @param taskId
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject getTaskDetails(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException;

	/**
	 * Gets the model for an external activity.
	 * 
	 * @param taskId
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject getExternalActivityModel(String externalActivityId)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Runs a service on the Process Server/Center. Any arguments required to
	 * execute the service must be passed in. If an error occurs, an exception
	 * is thrown.
	 * 
	 * @param serviceName
	 * @param arguments
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject runService(String serviceName, JSONObject arguments)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Resumes a service on the Process Server/Center. If an error occurs, an
	 * exception is thrown.
	 * 
	 * @param serviceName
	 * @param resumeKey
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject resumeService(int serviceId, String resumeKey)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Sets one or more variables of the specified service instance on the
	 * Process Server/Center. If an error occurs, an exception is thrown.
	 * 
	 * @param serviceName
	 * @param arguments
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject setData(int serviceId, JSONObject arguments)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Gets one or more variables from the specified service instance on the
	 * Process Server/Center. If an error occurs, an exception is thrown.
	 * 
	 * @param serviceName
	 * @param arguments
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject getData(int serviceId, String fields[])
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Starts a task.
	 * 
	 * @param taskId
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject startTask(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException;

	/**
	 * Assigns a task to the specified user. Note that reassigning a task is the
	 * equivalent of claiming a task.
	 * 
	 * @param taskId
	 * @param userid
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject assignTask(int taskId, String userid)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Assigns the task to the user who is submitting the request (i.e., the
	 * currently logged on user). Note that assigning a task is the equivalent
	 * of claiming a task.
	 * 
	 * @param taskId
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject assignTask(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException;

	/**
	 * Returns the task back to the original owner.
	 * 
	 * @param taskId
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject returnTask(int taskId) throws BPMClientException,
			AuthenticationTokenHandlerException;

	public JSONObject getInbox() throws BPMClientException,
			AuthenticationTokenHandlerException;

	public JSONObject getTasks(String bpdName) throws BPMClientException,
			AuthenticationTokenHandlerException;

	/**
	 * All parameters are optional (i.e., a null value can be passed in) but
	 * savedSearchName.
	 * 
	 * @param savedSearchName
	 * @param organization
	 * @param firstColumnSort
	 * @param secondColumnSort
	 * @return
	 * @throws BPMClientException
	 * @throws AuthenticationTokenHandlerException
	 */
	public JSONObject search(String columns[], String conditions[],
			String organization, String firstColumnSort, String secondColumnSort)
			throws BPMClientException, AuthenticationTokenHandlerException;

	/**
	 * Use this method to execute a JavaScript expression.
	 * 
	 * @param processId
	 * @param js
	 */
	public void executeJS(int processId, String js) throws BPMClientException,
			AuthenticationTokenHandlerException;

}
