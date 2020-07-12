import javax.*;
import hw.*;
import LiquidCrystal;
import LCDKeypadShield;

public class App extends Application {

    private LCDKeypadShield shield;
    private LiquidCrystal lcd;

    public App() throws IOException {
        //IO.setDate(20, 07, 12, 0);
        //IO.setTime(19, 10, 10);
        shield = new LCDKeypadShield();
        lcd = shield.getLCD();

        Config config = new Config() {

            public void selectKeyPressed() {
                showPressedButton("Select");
            }

            public void leftKeyPressed() {
                showPressedButton("Left");
            }

            public void upKeyPressed() {
                showPressedButton("Up");
            }

            public void downKeyPressed() {
                showPressedButton("Down");
            }

            public void rightKeyPressed() {
                showPressedButton("Right");
            }
        };
        shield.configure(config);
    }

    private void showPressedButton(String name) {
        lcd.setCursor(0, 1);
        lcd.print("Btn " + name);
    }

    public void onUpdate() {
        try {
            lcd.clear();

            int millis = System.currentTimeMillis() / 60000;

            lcd.print("UP " + millis + "m");

            lcd.print(" " + IO.getTime());

            shield.update();

            Thread.sleep(250);
        } catch (Exception ex) {
            Logger.e(ex.getMessage());
        }

    }
}
