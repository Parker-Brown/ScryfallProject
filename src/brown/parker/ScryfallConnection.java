package brown.parker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/*
 * Makes GET requests using a full URL or just a Scryfall query
 * 
 * NOTE: I have not tested Scryfall's advanced query options such as sorting and filters, and thus 
 * 		 Site specific syntax may be unstable (only pure text supported at this time)
 */
public class ScryfallConnection {
	private String lastResponse = null;
	private final String GET_URL_SEARCH = "https://api.scryfall.com/cards/search?q=";
	private final String GET_URL_EXACT = "https://api.scryfall.com/cards/named?exact=";
	
	
	/*
	 * Submit GET request given beginning partial URL TODO Merge with nextPage
	 */
	public String search(String query, boolean exact) {

		//replace space with %20
		query = query.replaceAll(" ", "%20");
		
		//--------------------set up a connection----------------------
		URL obj = null;
		HttpURLConnection connection = null;
		try {
			if(exact) {
				obj = new URL(GET_URL_EXACT + query);
			}
			else {
				obj = new URL(GET_URL_SEARCH + query);
			}
		} catch (MalformedURLException e) {
			System.out.println("ERROR: Bad URL");
			System.exit(1);
		}
		
		try {
			connection = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			System.out.println("ERROR: could not initialize connection");
			System.exit(1);
		}
		
		try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			System.out.println("ERROR: cannot use GET request method");
			System.exit(1);
		}
		
		//-------------------test response code-----------
		int responseCode = -32; //ret vals -1,200,401
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			System.out.println("ERROR: cannot get response code");
			System.exit(1);
		}
		
		if(responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch (IOException e1) {
				System.out.println("ERROR: connection input stream unavailable");
				System.exit(1);
			}
			String line;
			StringBuffer response = new StringBuffer();
			
			try {
				while((line = in.readLine()) != null) {
					response.append(line);
				}
			} catch (IOException e) {
				System.out.println("ERROR: IO Exception on parsing");
				System.exit(1);
			}
			
			lastResponse = response.toString();
			return lastResponse;
		}
		else {
			System.out.println("No searches matched that name.\n");
			return null;			//stop complaining compiler
		}
	}
	
	
	/*
	 * 	submit GET request to full URL
	 */
	public String nextPage(String url) {
		
		//for some reason scryfall uses char code \u0026 for & but it doesn't work unless converted back...
		url = url.replaceAll("\\\\u0026", "&");
		//--------------------set up a connection----------------------
		URL obj = null;
		HttpURLConnection connection = null;
		try {
			obj = new URL(url);
		} catch (MalformedURLException e) {
			System.out.println("ERROR: Bad URL");
			System.exit(1);
		}
		
		try {
			connection = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			System.out.println("ERROR: could not initialize connection");
			System.exit(1);
		}
		
		try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			System.out.println("ERROR: cannot use GET request method");
			System.exit(1);
		}
		
		//-------------------test response code-----------
		int responseCode = -32; //ret vals -1,200,401
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			System.out.println("ERROR: cannot get response code");
			System.exit(1);
		}
		
		if(responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch (IOException e1) {
				System.out.println("ERROR: connection input stream unavailable");
				System.exit(1);
			}
			String line;
			StringBuffer response = new StringBuffer();
			
			try {
				while((line = in.readLine()) != null) {
					response.append(line);
				}
			} catch (IOException e) {
				System.out.println("ERROR: IO Exception on parsing");
				System.exit(1);
			}
			
			lastResponse = response.toString();
			return lastResponse;
		}
		else {
			System.out.println("API contained faulty page link");
			System.exit(1);
			return null;			//stop complaining compiler
		}
	}
}
