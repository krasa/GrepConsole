import org.junit.jupiter.api.Test;

public class MainTest {

	@Test
	public void testSend() throws Exception {
		Main.send("8095", "D:\\foo bar\\foo bar.txt");
	}
}
