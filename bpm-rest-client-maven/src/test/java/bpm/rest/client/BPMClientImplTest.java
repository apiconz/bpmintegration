package bpm.rest.client;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import bpm.rest.client.authentication.AuthenticationTokenHandler;
import bpm.rest.client.authentication.AuthenticationTokenHandlerException;
import bpm.rest.client.authentication.was.WASAuthenticationTokenHandler;

public class BPMClientImplTest {

	BPMClient client;

	@Before
	public void setUp() throws Exception {

		AuthenticationTokenHandler handler = new WASAuthenticationTokenHandler(
				"admin", "admin");

		client = new BPMClientImpl("wp8.onp.gob.pe", 80, handler);

	}

	@Test
	public void testGetExposedProcess() {
		JSONObject result = null;
		try {
			result = client.getExposedProcess();
		} catch (BPMClientException e) {
			fail("BPMClientException");
		} catch (AuthenticationTokenHandlerException e) {
			fail("AuthenticationTokenHandlerException");
		}
		
		System.out.println(result.toString());
		assertNotNull(result);
	}

}
