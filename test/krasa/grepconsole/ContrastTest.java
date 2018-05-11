package krasa.grepconsole;

public class ContrastTest {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("FATAL \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.out.println("ERROR \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.out.println("WARN \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.out.println("INFO \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.out.println("DEBUG \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.out.println("TRACE \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		Thread.sleep(100);
		System.err.println();
		Thread.sleep(100);
		System.err.println("FATAL \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.err.println("ERROR \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.err.println("WARN \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.err.println("INFO \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.err.println("DEBUG \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		System.err.println("TRACE \tat com.company.Kuk.main(Kuk.java:12)    http://www.slf4j.org/codes.html#StaticLoggerBinder ");
		Thread.sleep(100);

		System.err.println();
		Thread.sleep(100);

		System.out.println("FATAL \tat java.lang.String.contains(String.java:12)");
		System.out.println("ERROR \tat java.lang.String.contains(String.java:12)");
		System.out.println("WARN \tat java.lang.String.contains(String.java:12)");
		System.out.println("INFO \tat java.lang.String.contains(String.java:12)");
		System.out.println("DEBUG \tat java.lang.String.contains(String.java:12)");
		System.out.println("TRACE \tat java.lang.String.contains(String.java:12)");
		Thread.sleep(100);
		System.err.println();
		Thread.sleep(100);
		System.err.println("FATAL \tat java.lang.String.contains(String.java:12)");
		System.err.println("ERROR \tat java.lang.String.contains(String.java:12)");
		System.err.println("WARN \tat java.lang.String.contains(String.java:12)");
		System.err.println("INFO \tat java.lang.String.contains(String.java:12)");
		System.err.println("DEBUG \tat java.lang.String.contains(String.java:12)");
		System.err.println("TRACE \tat java.lang.String.contains(String.java:12)");
	}
}