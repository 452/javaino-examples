import ilcd. * ;
import javax.events. * ;
import javax. * ;
import hw. * ;
import comm. * ;
import java.util. * ;
import java.io.File;

public class App extends Application implements OnTouchListener {

	public App() throws Exception {
		General.resetAll();
		Touch.setTouchFieldReportingEnabled(true);
		EventManagement.getTouchEventDispatcher().addListener(this);
		Size displaySize = Control.getDisplaySize();
		Touch.setTouchFieldWidth(displaySize.getWidth());
		Touch.setTouchFieldHeight(displaySize.getHeight());
		Touch.createDefineTouchField(1, 0);
		scan();
	}

	public void onTouch(TouchEvent event) {
		if (event.isTouchPressed()) {
			scan();
		}
	}

	private void scan() {
		clear();
		println("SDCard");
		File root = new File("/");
		println("Mounted: " + root.isMounted());
		println("Total Space: " + root.getTotalSpace());
		println("Free Space: " + root.getFreeSpace());
		walk("/");
	}
	public void walk(String path) {
		try {
			File root = new File(path);
			File[] list = root.listFiles();

			if (list == null) return;

			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					walk(list[i].getAbsolutePath());
					println("Dir: " + list[i].getAbsolutePath());
				} else {
					println("File: " + list[i].getCanonicalPath());
				}
			}
		}
		catch(Exception exc) {}
	}

	public void onUpdate() {

}

	private void println(String text) {
		print(text + "\r");
	}

	private void print(String text) {
		try {
			Draw.writeText(text);
		} catch(Exception ex) {}
	}

	private void clear() {
		try {
			Draw.eraseDisplay();
		}
		catch(Exception exc) {}
	}
}
