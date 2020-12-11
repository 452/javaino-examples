import javax.*;
import comm.*;
import hw.*;
import BME280.Config;
import BME280;

public class App extends Application {

    private I2C i2c;
    private BME280 bme280;

    public App() throws Exception, I2CException {

        i2c = new I2C(0x76, 400000);

        Config config = new Config() {

            public int read(int address) {
                return i2cRead(address);
            }

            public void read(int address, byte[] buffer, int length) {
                i2cRead(address, buffer, length);
            }

            public void write(int address, byte command) {
                try {
                    int ret = i2c.start();
                    if (ret == I2C.ERROR)
                        Console.println("I2C Error");
                    if (ret == I2C.TIMEOUT)
                        Console.println("I2C Timeout");
                    if (ret == I2C.OK) {
                        i2c.write(new byte[] {
                            (byte) command
                        }, (byte) address);
                    }
                    i2c.stop();
                } catch (Exception exc) {
                    Console.println("write operation - error");
                }
            }
        };
        bme280 = new BME280(config);
    }

    public void onUpdate() {
        try {
            bme280.calc();
            Logger.log("Temperature in Celsius : " + Math.round(bme280.getTemperature()) + "°C");
            Logger.log("Pressure : " + Math.round(bme280.getPressure()) + " hPa");
            Logger.log("Relative Humidity : " + Math.round(bme280.getHumidity()) + "% RH");
            delay(1000);
        } catch (Exception ex) {
            Logger.e(ex.getMessage());
        }
    }

    int i2cRead(int address) {
        byte[] buffer = {
            0
        };

        try {
            int ret = i2c.start();
            if (ret == I2C.ERROR)
                Console.println("I2C Error");
            if (ret == I2C.TIMEOUT)
                Console.println("I2C Timeout");
            if (ret == I2C.OK) {
                i2c.write(new byte[] {
                    (byte) address
                });

                i2c.reStart();
                delay(25);

                i2c.read(buffer);
            }
            i2c.stop();
        } catch (Exception exc) {
            Console.println("write operation - error");
        }

        return buffer[0];
    }

    public void i2cRead(int address, byte[] buffer, int length) {
        try {
            int ret = i2c.start();
            if (ret == I2C.ERROR)
                Console.println("I2C Error");
            if (ret == I2C.TIMEOUT)
                Console.println("I2C Timeout");
            if (ret == I2C.OK) {
                i2c.write(new byte[] {
                    (byte) address
                });

                i2c.reStart();
                delay(25);

                i2c.read(buffer, 0, length);
            }
            i2c.stop();
        } catch (Exception exc) {
            Console.println("write operation - error");
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
