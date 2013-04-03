package krasa.grepconsole;

public class Cloner {
	public static <T> T deepClone(T o) {
		com.rits.cloning.Cloner cloner = new com.rits.cloning.Cloner();
		cloner.setNullTransient(true);
		return cloner.deepClone(o);
	}
}
