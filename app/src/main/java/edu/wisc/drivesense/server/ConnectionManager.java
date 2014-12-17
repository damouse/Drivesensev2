package edu.wisc.drivesense.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;

/**
 * Responsible for any and all interactions with the API.
 *
 * NOTE: add checks for network avaliability
 *
 * HTTP lib: http://loopj.com/android-async-http/
 *
 * Refactor the preferences into this class
 * Refactor the async tasks to rely on finishedExecting (or whatever its called)
 * @author Damouse
 */
public class ConnectionManager {
	private static final String TAG = "ConnectionManager";
	private static final String SERVER_URL = "https://knowmydrive.com/";

	private Context context;
    private ConnectionManagerCallback delegate;

	public ConnectionManager(Context context, ConnectionManagerCallback delegate) {
		this.context = context;
        this.delegate = delegate;
	}

/* Public Methods */
	/**
	 * Log the user in. Saves the auth key to shared preferences. Notifies the caller
	 * thruogh delegate methods detailed in ConnectionManagerCallback.
	 *
	 * @param email
	 * @param password
	 */
	public void logIn(String email, String password) {
//		AsyncHttpClient client = new AsyncHttpClient();
//		String url = SERVER_URL + "mobile_login?user_email=" + email + "&password=" + password;
//
//		client.get(url, new AsyncHTTPResponseCallback(delegate) {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
//                super.onSuccess(statusCode, headers, rawResponse);
//                String response = new String(rawResponse);
//
//                //Parse response, deserialize into an object, and pass it back
//                Gson gson = new Gson();
//                User user = gson.fromJson(response, User.class);
//
//                onLoginCompletion(true, user, null);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                super.onFailure(statusCode, headers, errorResponse, e);
//                onLoginCompletion(false, null, extractResponse(errorResponse));
//            }
//		});
	}


    /**
     * Register the user with the passed credentials.
     */
    public void register(String email, String password) {
//        JSONObject json = new JSONObject();
//
//        try {
//            json.put("user_email", email);
//            json.put("password", password);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            delegate.onLoginCompletion(false, null, "Parse Error");
//        }
//
//        createAndPostRequest("mobile_register", json.toString(), new AsyncHTTPResponseCallback(delegate) {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
//                super.onSuccess(statusCode, headers, rawResponse);
//                String response = new String(rawResponse);
//
//                //Parse response, deserialize into an object, and pass it back
//                Gson gson = new Gson();
//                User user = gson.fromJson(response, User.class);
//
//                onLoginCompletion(true, user, null);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                super.onFailure(statusCode, headers, errorResponse, e);
//                onLoginCompletion(false, null, extractResponse(errorResponse));
//            }
//        });
    }

    /**
     * Check the user's session token and credentials to make sure the login is valid
     */
    public void checkSession(User user) {
//        AsyncHttpClient client = new AsyncHttpClient();
//        String url = SERVER_URL + "sess?user_email=" + user.email + "&user_token=" + user.authentication_token;
//
//        client.get(url, new AsyncHTTPResponseCallback(delegate) {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
//                super.onSuccess(statusCode, headers, rawResponse);
//                onSessionCompletion(true);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                super.onFailure(statusCode, headers, errorResponse, e);
//                onSessionCompletion(false);
//            }
//        });
    }

	/**
	 * Convert and upload the trip. The conversion is done in a parallel async task
	 * @param trip
	 */
	public void convertUploadTrip(final Trip trip, User user) {
//        new ConvertTripToJson() {
//            protected void onPostExecute(String json) {
//                createAndPostRequest("upload", json, new AsyncHTTPResponseCallback(delegate) {
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
//                        super.onSuccess(statusCode, headers, rawResponse);
//                        new Sugar(context).setTripUploaded(trip);
//
//                        onUploadCompletion(true, new String(rawResponse));
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                        super.onFailure(statusCode, headers, errorResponse, e);
//                        onUploadCompletion(false, extractResponse(errorResponse));
//                    }
//                });
//            }
//        }.execute(new Object[] {trip, user});
	}

    /**
     * Ping task. Call back to the mothership with this device's IP address, UDID, and power levels.
     */
    public void logDeviceWithServer(String udid, String ip_address, float power, int frequency) {
//        JsonObject json = new JsonObject();
//
//        json.addProperty("udid", udid);
//        json.addProperty("ip_address", ip_address);
//        json.addProperty("power", power);
//        json.addProperty("frequency", frequency);
//
//        createAndPostRequest("log_device", json.toString(), new AsyncHTTPResponseCallback(delegate) {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
//                super.onSuccess(statusCode, headers, rawResponse);
////                new DatabaseManager(context).setTripUploaded(trip);
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                super.onFailure(statusCode, headers, errorResponse, e);
//            }
//        });
    }

	/**
	 * Async task, converts trip to json and calls back to the connection manager with completed string.
	 *
	 * @author Damouse
	 */
	class ConvertTripToJson extends AsyncTask<Object, Integer, String> {
		@Override
		protected String doInBackground(Object... params) {
            Trip trip = (Trip) params[0];
            User user = (User) params[1];

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

            JsonObject json = new JsonObject();
            json.add("trip", gson.toJsonTree(trip));
            json.add("user", gson.toJsonTree(user));

            Log.d(TAG, json.toString());
            return json.toString();
		}
    }


    /* Utility methods and classes for connections */
    private String extractResponse(byte[] rawResponse) {
        String ret = "";

        if (rawResponse == null)
            return "server error";

        try {
            JSONObject json = new JSONObject(new String(rawResponse));
            return (String) json.get("response");
        } catch (JSONException e1) {
            e1.printStackTrace();
            return "server error";
        }
    }

    /**
     * Creates a JSON-payload post request and returns it
     */
    private void createAndPostRequest(String url, String body, AsyncHTTPResponseCallback callback) {
//        AsyncHttpClient client = new AsyncHttpClient();
//        ByteArrayEntity entity = null;
//        String urlPath = SERVER_URL + url;
//
//        client.addHeader("Accept", "application/json");
//
//        try {
//            entity = new ByteArrayEntity(body.getBytes("UTF-8"));
//        } catch (UnsupportedEncodingException e1) {
//            e1.printStackTrace();
//            client = null;
//        }
//
//        client.post(context, urlPath, entity, "Application/json", callback);
    }

	/**
	 * A subclass of the stock response handler that implements a callback field.
     *
     * The stock methods here simply log the appropriate information if the local flag is set.
	 * @author Damouse
	 *
	 */
	class AsyncHTTPResponseCallback {
//		private ConnectionManagerCallback delegate;
//        private static final boolean LOG_CONNECTIONS = true;
//
//		public AsyncHTTPResponseCallback(ConnectionManagerCallback delegate) {
//			this.delegate = delegate;
//		}
//
//        @Override
//        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
//            if(!LOG_CONNECTIONS)
//                return;
//
//            Log.d(TAG, "Status Code: " + statusCode);
//            for(int i = 0; i < headers.length; i++)
//                Log.d(TAG, "Header: " + headers[i].getName() + " " + headers[i].getValue());
//
//            Log.d(TAG, new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(new String(response))));
//        }
//
//        @Override
//        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//            if (!LOG_CONNECTIONS)
//                return;
//
//            Log.d(TAG, "Status Code:" + statusCode);
//
//            if (headers != null) {
//                for (int i = 0; i < headers.length; i++)
//                    Log.d(TAG, "Header: " + headers[i].getName() + " " + headers[i].getValue());
//            }
//
//            Log.d(TAG, "Error: " + e.toString());
//
//            if (errorResponse != null) {
//                Log.d(TAG, new String(errorResponse));
//            }
//        }
//
//
//        /* Callbacks */
//		protected void onLoginCompletion(boolean success, User user, String response) {
//            if (delegate != null)
//                delegate.onLoginCompletion(success, user, response);
//		}
//
//		protected void onUploadCompletion(boolean success, String response) {
//			if (delegate != null)
//                delegate.onUploadCompletion(success, response);
//		}
//
//        protected void onSessionCompletion(boolean success) {
//            if (delegate != null)
//                delegate.onSessionCompletion(success); }
	}
}