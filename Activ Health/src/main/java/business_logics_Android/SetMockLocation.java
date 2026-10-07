package business_logics_Android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.html5.Location;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;





public class SetMockLocation implements Nlp {
	@InputParams({ @InputParam(name = "Lat", type = "java.lang.Double") ,@InputParam(name = "Long", type = "java.lang.Double") })
	// @ReturnType(name = "trueOrFalse", type = "java.lang.Boolean")

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@SuppressWarnings("deprecation")
	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Double lat = (Double) attributes.get("Lat");
		Double longg = (Double) attributes.get("Long");

		try {
			AndroidDriver driver = nlpRequestModel.getAndroidDriver();
//			List l = SetMockLocation.getGeoLocation(address);

			driver.setLocation(new Location(lat, longg, 0));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Location is successfully set ");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
		//	log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed To set the location " + exceptionAsString);
		}
		return nlpResponseModel;
	}

	public static List getGeoLocation(String s) throws JSONException {
		List<Double> l = new ArrayList();
		try {
			// Specify the address for which you want to get the latitude and longitude
			String address = s;

			// Construct the URL for the Nominatim API request
			String url = "https://nominatim.openstreetmap.org/search?q=" + address.replace(" ", "+")
					+ "&format=json&limit=1";

			// Send an HTTP GET request to the Nominatim API endpoint
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");

			// Read the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			// Parse the JSON response
			JSONArray jsonArray = new JSONArray(response.toString());
			if (jsonArray.length() > 0) {
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				double latitude = jsonObject.getDouble("lat");
				l.add(latitude);
				double longitude = jsonObject.getDouble("lon");
				l.add(longitude);
				System.out.println("Latitude: " + latitude);
				System.out.println("Longitude: " + longitude);
			} else {
				System.out.println("No results found for the given address.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return l;
	}

}
