import hw.IO;
import hw.IOException;
import LiquidCrystal;

public class LCDKeypadShield {

    private Config config;
    private LiquidCrystal lcd;

    public LCDKeypadShield() throws IOException {
        lcd = new LiquidCrystal();
        lcd.home();
        lcd.clear();
    }

    public LiquidCrystal getLCD() {
        return lcd;
    }

    public void update() throws IOException {
        if (config == null)
            return;

        int adcKeyIn = IO.getADCValue(IO.ANALOG_IN_0) >> 2;

        if (adcKeyIn > 1000) return; // We make this the 1st option for speed reasons since it will be the most likely result
        // For V1.1 us this threshold
        // need calibrate for your board
        if (adcKeyIn < 50) {
            config.rightKeyPressed();
            return;
        }
        if (adcKeyIn < 250) {
            config.upKeyPressed();
            return;
        };
        if (adcKeyIn < 470) {
            config.downKeyPressed();
            return;
        }
        if (adcKeyIn < 730) {
            config.leftKeyPressed();
            return;
        }
        if (adcKeyIn < 850) {
            config.selectKeyPressed();
        }
    }

    public void configure(Config config) {
        this.config = config;
    }
}

interface Config {
    void selectKeyPressed();
    void leftKeyPressed();
    void upKeyPressed();
    void downKeyPressed();
    void rightKeyPressed();
}
