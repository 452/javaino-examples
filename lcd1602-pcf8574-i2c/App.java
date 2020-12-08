import javax.*;
import comm.*;
import hw.*;
import LcdPcf8574.Config;
import LcdPcf8574;

public class App extends Application {

    private I2C i2c;
    private LcdPcf8574 lcd;

    public App() throws Exception, I2CException {

        i2c = new I2C(0x27, 400000);

        Config config = new Config() {
            public void write(byte[] data, int command) {
                try {
                    int ret = i2c.start();
                    if (ret == I2C.ERROR)
                        Console.println("I2C Error");
                    if (ret == I2C.TIMEOUT)
                        Console.println("I2C Timeout");
                    if (ret == I2C.OK) {
                        i2c.write(data, (byte) command);
                    }
                    i2c.stop();
                } catch (Exception exc) {
                    Console.println("write operation - error");
                }
            }
        };
        lcd = new LcdPcf8574(config);
        lcd.begin(16, 2);
        lcd.setBacklight(true);
        lcd.home();
    }

    public void onUpdate() {
        try {
            lcd.clear();
				lcd.print("Hi " + IO.getTime());
            delay(1000);
            lcd.setCursor(15, 0);
            int[] heart = {
                0,
                10,
                31,
                31,
                31,
                14,
                4,
                0
            };
            lcd.createChar(0, heart);
            lcd.setCursor(15, 0);
            lcd.write(0); // write :heart: custom character

            delay(1000);
            lcd.setBacklight(false);
            delay(400);
            lcd.setBacklight(true);
            delay(2000);

            lcd.clear();
            lcd.print("Cursor On");
            lcd.cursor();
            delay(2000);

            lcd.clear();
            lcd.print("Cursor Blink");
            lcd.blink();
            delay(2000);

            lcd.clear();
            lcd.print("Cursor OFF");
            lcd.noBlink();
            lcd.noCursor();
            delay(2000);

            lcd.clear();
            lcd.print("Display Off");
            lcd.noDisplay();
            delay(2000);

            lcd.clear();
            lcd.print("Display On");
            lcd.display();
            delay(2000);

            lcd.clear();
            lcd.setCursor(0, 0);
            lcd.print("*** first line.");
            lcd.setCursor(0, 1);
            lcd.print("*** second line.");
            delay(2000);

            lcd.scrollDisplayLeft();
            delay(2000);

            lcd.scrollDisplayLeft();
            delay(2000);

            lcd.scrollDisplayLeft();
            delay(2000);

            lcd.scrollDisplayRight();
            delay(2000);
        } catch (Exception ex) {
            Logger.e(ex.getMessage());
        }
    }

    private void delay(int microseconds) {
        try {
            Thread.sleep(microseconds);
        } catch (InterruptedException e) {
            Logger.e("Sleep error");
        }
    }
}
