import ilcd. * ;
import javax.events. * ;
import javax. * ;
import hw. * ;
import comm. * ;
import java.util. * ;

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
		int devicesCount = 0;
		int line = 1;
		println("i2c scanner");
		println("     0   1   2   3   4   5   6   7   8   9   A   B   C   D   E  F");
		print("00:              ");
		for (int address = 0x03; address < 0x78; address++) {
			if (isAvailable(address)) {
				print(" ");
				if (address < 16) {
					print("0");
				}
				print(Integer.toHexString(address));
				devicesCount++;
			} else {
				print("  --");
			}

			if ((address + 1) % 16 == 0) {
				println("");
				print(String.valueOf(line));
				print("0:");
				line++;
			}

		}
		if (devicesCount == 0) {
			println("\rNo i2c devices found");
		} else {
			println("\rDone, found " + devicesCount + " device/s");
		}
	}

	private boolean isAvailable(int address) {
		boolean status = false;
		try {
			I2C i2c = new I2C(address);
			int ret = i2c.start();
			if (ret == I2C.OK) {
				status = i2c.read() > 0;
			}
			i2c.stop();
		} catch(Exception exc) {}
		return status;
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
