import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * @author Vojtech Krasa
 */
public class Main {
	public static void main(String args[]) throws InterruptedException, UnsupportedEncodingException {
		System.out.println(Arrays.toString(args));
		if (args != null && args.length == 2) {
			final String port = args[0];
			final String file = args[1];
			send(port, file);
		}
	}

	public static void send(String port, String file) throws UnsupportedEncodingException {
		String encodedFile = URLEncoder.encode(file, "UTF-8");
		Main c = new Main();
		c.get("http://localhost:" + port + "?message=" + encodedFile);
	}

	public String get(String urlToRead) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
