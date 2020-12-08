import javax.*;
import hw.*;

public class MainClass {

	public static void main(String[] args) {
		try {
			App app = new App();
			app.run();
		} catch(MainThreadException ex) {
			Logger.e(ex.getMessage());
		} catch(Exception ex) {
			Logger.e(ex.getMessage());
		}
	}
}
