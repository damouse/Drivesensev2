package edu.wisc.drivesense.server;

import edu.wisc.drivesense.model.User;

/**
 * A callback interface for the connection manager
 * @author Damouse
 *
 */
public interface ConnectionManagerCallback {
	public void onLoginCompletion(boolean success, User user, String response);

	public void onUploadCompletion(boolean success, String response);

    public void onSessionCompletion(boolean success);
}