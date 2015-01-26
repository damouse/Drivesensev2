package edu.wisc.drivesense.server;

import edu.wisc.drivesense.model.MappableEvent;
import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.util.Date;

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
    //private static final String SERVER_URL = "128.105.32.102:3000";

	private Context context;

    public ConnectionManager(Context context) {
        this.context = context;
	}


    /**
     * Issue login api call. Does not perform the logging in (as it pertains to the models)
     */
    public void logIn(String email, String password, final ConnectionManagerCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("user_email", email);
        json.addProperty("password", password);

        createAndPostRequest("mobile_login", json.toString(), new AsyncHTTPResponseCallback(callback) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
                String response = new String(rawResponse);

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                User user = gson.fromJson(response, User.class);

                if (callback != null)
                    callback.onConnectionCompleted(user);
            }
        });
    }


    /**
     * Register the user with the passed credentials.
     */
    public void register(String email, String password, final ConnectionManagerCallback callback) {
        JSONObject json = new JSONObject();

        try {
            json.put("user_email", email);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onConnectionFailed("error parsing email or password");
            return;
        }

        createAndPostRequest("mobile_register", json.toString(), new AsyncHTTPResponseCallback(callback) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
                super.onSuccess(statusCode, headers, rawResponse);
                String response = new String(rawResponse);

                Gson gson = new Gson();
                User user = gson.fromJson(response, User.class);

                if (callback != null)
                    callback.onConnectionCompleted(user);
            }
        });
    }

    /**
     * Check the user's session token and credentials to make sure the login is valid
     */
    public void checkSession(User user, final ConnectionManagerCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = SERVER_URL + "sess?user_email=" + user.email + "&user_token=" + user.authenticationToken;

        client.get(url, new AsyncHTTPResponseCallback(callback) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
                super.onSuccess(statusCode, headers, rawResponse);
                if (callback != null)
                    callback.onConnectionCompleted(true);
            }
        });
    }

	/**
	 * Convert and upload the trip. The conversion is done in a parallel async task
	 * @param trip
	 */
    public void convertUploadTrip(final Trip trip, User user, final ConnectionManagerCallback callback) {
        new ConvertTripToJson() {
            protected void onPostExecute(String json) {
//                createAndPostRequest("upload", json, new AsyncHTTPResponseCallback(callback) {
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] rawResponse) {
//                        super.onSuccess(statusCode, headers, rawResponse);
//                        trip.setUploaded(true);
//                        trip.save();
//
//                        if (callback != null)
//                            callback.onConnectionCompleted(true);
//                    }
//                });
            }
        }.execute(trip, user);
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
        AsyncHttpClient client = new AsyncHttpClient();
        ByteArrayEntity entity = null;
        String urlPath = SERVER_URL + url;

        client.addHeader("Accept", "application/json");

        try {
            entity = new ByteArrayEntity(body.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            client = null;
            //do something here
        }

        client.post(context, urlPath, entity, "Application/json", callback);
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

            trip.mappable_events = trip.getEvents();
            trip.time_stamp = new Date(trip.timestamp);

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

            JsonObject json = new JsonObject();
            json.add("trip", gson.toJsonTree(trip));
            json.add("user", gson.toJsonTree(user));

            Log.d(TAG, json.toString());
            return json.toString();
        }
    }

	/**
	 * A subclass of the stock response handler that implements a callback field.
     *
     * The stock methods here simply log the appropriate information if the local flag is set.
	 * @author Damouse
     *
     */
    class AsyncHTTPResponseCallback extends AsyncHttpResponseHandler {
        private static final boolean LOG_CONNECTIONS = true;
        private ConnectionManagerCallback delegate;

        public AsyncHTTPResponseCallback(ConnectionManagerCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            if (!LOG_CONNECTIONS)
                return;

            Log.d(TAG, "Status Code: " + statusCode);
            for (int i = 0; i < headers.length; i++)
                Log.d(TAG, "Header: " + headers[i].getName() + " " + headers[i].getValue());

            Log.d(TAG, new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(new String(response))));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
            String response = null;
            if (errorResponse != null) {
                try {
                    JSONObject json = new JSONObject(new String(errorResponse));
                    response = json.getString("response");
                }
                catch (Exception exception) {
                    Log.e(TAG, "An error occured parsing the return string");
                    response = null;
                }
            }

            if (LOG_CONNECTIONS) {

                Log.d(TAG, "Status Code:" + statusCode);

                if (headers != null) {
                    for (int i = 0; i < headers.length; i++)
                        Log.d(TAG, "Header: " + headers[i].getName() + " " + headers[i].getValue());
                }

                Log.d(TAG, "Error: " + e.toString());

                if (errorResponse != null) {
                    Log.d(TAG, new String(errorResponse));
                }
            }


            //pull the response message out of the return and pass it back up to the caller
            if (response == null)
                delegate.onConnectionFailed("something has gone wrong");
            else
                delegate.onConnectionFailed(response);
        }
    }


    /* Callback interface */
    public interface ConnectionManagerCallback {
        public void onConnectionCompleted(Object... result);
        public void onConnectionFailed(String message);
    }

}