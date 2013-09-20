package bpm.rest.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BPMClientProxy implements InvocationHandler {
	private BPMClient bpmClient;

	private final static Logger LOGGER = Logger.getLogger(BPMClientProxy.class
			.getName());

	private static final int MAX_RETRY_ATTEMPTS = 2;

	public static BPMClient newInstance(BPMClient bpmClient) {
		return (BPMClient) java.lang.reflect.Proxy.newProxyInstance(bpmClient
				.getClass().getClassLoader(), bpmClient.getClass()
				.getInterfaces(), new BPMClientProxy(bpmClient));
	}

	private BPMClientProxy(BPMClient bpmClient) {
		this.bpmClient = bpmClient;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		InvocationTargetException exception = null;
		for (int i = 0; i < MAX_RETRY_ATTEMPTS; i++) {
			try {
				return method.invoke(bpmClient, args);
			} catch (InvocationTargetException itException) {
				exception = itException;
				if (exception.getTargetException() instanceof BPMClientException) {
					BPMClientException bpmException = (BPMClientException) exception
							.getTargetException();
					LOGGER.log(Level.SEVERE, bpmException.getMessage());
					if ((bpmException.getStatusCode() == 1001 || bpmException
							.getStatusCode() == 1000)) {
						LOGGER.log(Level.SEVERE,
								"Retrying BPMClient call... noRetryAttempts: "
										+ (i + 1));
					} else {
						break;
					}
				} else {
					break;
				}
			}
		}
		throw exception.getTargetException();
	}
}
