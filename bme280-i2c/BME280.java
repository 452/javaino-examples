import hw.Logger;
import java.io.IOException;

public class BME280 {

    public static final int ADDRESS = 0x76;
    //    private static final int COMMAND_RESET = 0;
    private static final int BME280_REG_ID = 0xD0;
    //    private static final int BME280_REG_SOFTRESET = 0xE0;
    //    private static final int BME280_SOFTRESET_VALUE = 0xB6;

    private Config config;
    private float temperature;
    private float humidity;
    private float pressure;

    public BME280(Config config) throws IOException {
        this.config = config;
        Logger.log("BME280 ID byte: 0x" + Integer.toHexString(read(BME280_REG_ID)));
    }

    public void calc() throws Exception {
        // Read 24 bytes of data from address 0x88(136)
        byte[] b1 = new byte[24];

        read(0x88, b1);
        // Convert the data
        // temp coefficients
        int dig_T1 = (b1[0] & 0xFF) + ((b1[1] & 0xFF) * 256);
        int dig_T2 = (b1[2] & 0xFF) + ((b1[3] & 0xFF) * 256);
        if (dig_T2 > 32767) {
            dig_T2 -= 65536;
        }
        int dig_T3 = (b1[4] & 0xFF) + ((b1[5] & 0xFF) * 256);
        if (dig_T3 > 32767) {
            dig_T3 -= 65536;
        }

        // pressure coefficients
        int dig_P1 = (b1[6] & 0xFF) + ((b1[7] & 0xFF) * 256);
        int dig_P2 = (b1[8] & 0xFF) + ((b1[9] & 0xFF) * 256);
        if (dig_P2 > 32767) {
            dig_P2 -= 65536;
        }
        int dig_P3 = (b1[10] & 0xFF) + ((b1[11] & 0xFF) * 256);
        if (dig_P3 > 32767) {
            dig_P3 -= 65536;
        }
        int dig_P4 = (b1[12] & 0xFF) + ((b1[13] & 0xFF) * 256);
        if (dig_P4 > 32767) {
            dig_P4 -= 65536;
        }
        int dig_P5 = (b1[14] & 0xFF) + ((b1[15] & 0xFF) * 256);
        if (dig_P5 > 32767) {
            dig_P5 -= 65536;
        }
        int dig_P6 = (b1[16] & 0xFF) + ((b1[17] & 0xFF) * 256);
        if (dig_P6 > 32767) {
            dig_P6 -= 65536;
        }
        int dig_P7 = (b1[18] & 0xFF) + ((b1[19] & 0xFF) * 256);
        if (dig_P7 > 32767) {
            dig_P7 -= 65536;
        }
        int dig_P8 = (b1[20] & 0xFF) + ((b1[21] & 0xFF) * 256);
        if (dig_P8 > 32767) {
            dig_P8 -= 65536;
        }
        int dig_P9 = (b1[22] & 0xFF) + ((b1[23] & 0xFF) * 256);
        if (dig_P9 > 32767) {
            dig_P9 -= 65536;
        }
        //
        //         // Read 1 byte of data from address 0xA1(161)
        int dig_H1 = ((byte) read(0xA1) & 0xFF);
        //
        //         // Read 7 bytes of data from address 0xE1(225)
        read(0xE1, b1, 7);
        //
        //         // Convert the data
        //         // humidity coefficients
        int dig_H2 = (b1[0] & 0xFF) + (b1[1] * 256);
        if (dig_H2 > 32767) {
            dig_H2 -= 65536;
        }
        int dig_H3 = b1[2] & 0xFF;
        int dig_H4 = ((b1[3] & 0xFF) * 16) + (b1[4] & 0xF);
        if (dig_H4 > 32767) {
            dig_H4 -= 65536;
        }
        int dig_H5 = ((b1[4] & 0xFF) / 16) + ((b1[5] & 0xFF) * 16);
        if (dig_H5 > 32767) {
            dig_H5 -= 65536;
        }
        int dig_H6 = b1[6] & 0xFF;
        if (dig_H6 > 127) {
            dig_H6 -= 256;
        }

        // Select control humidity register
        // Humidity over sampling rate = 1
        write(0xF2, (byte) 0x01);
        //          Select control measurement register
        //          Normal mode, temp and pressure over sampling rate = 1
        write(0xF4, (byte) 0x27);
        // Select config register
        // Stand_by time = 1000 ms
        write(0xF5, (byte) 0xA0);

        // Read 8 bytes of data from address 0xF7(247)
        // pressure msb1, pressure msb, pressure lsb, temp msb1, temp msb, temp lsb, humidity lsb, humidity msb
        byte[] data = new byte[8];
        read(0xF7, data, 8);

        // Convert pressure and temperature data to 19-bits
        int adc_p = (((int)(data[0] & 0xFF) * 65536) + ((int)(data[1] & 0xFF) * 256) + (int)(data[2] & 0xF0)) / 16;
        int adc_t = (((int)(data[3] & 0xFF) * 65536) + ((int)(data[4] & 0xFF) * 256) + (int)(data[5] & 0xF0)) / 16;
        // Convert the humidity data
        int adc_h = ((int)(data[6] & 0xFF) * 256 + (int)(data[7] & 0xFF));

        // Temperature offset calculations
        float var1 = (((float) adc_t) / 16384.0f - ((float) dig_T1) / 1024.0f) * ((float) dig_T2);
        float var2 = ((((float) adc_t) / 131072.0f - ((float) dig_T1) / 8192.0f) *
            (((float) adc_t) / 131072.0f - ((float) dig_T1) / 8192.0f)) * ((float) dig_T3);
        float t_fine = (int)(var1 + var2);
        float cTemp = (var1 + var2) / 5120.0f;
        float fTemp = cTemp * 1.8f + 32;

        // Pressure offset calculations
        var1 = (t_fine / 2.0f) - 64000.0f;
        var2 = var1 * var1 * ((float) dig_P6) / 32768.0f;
        var2 = var2 + var1 * ((float) dig_P5) * 2.0f;
        var2 = (var2 / 4.0f) + (((float) dig_P4) * 65536.0f);
        var1 = (((float) dig_P3) * var1 * var1 / 524288.0f + ((float) dig_P2) * var1) / 524288.0f;
        var1 = (1.0f + var1 / 32768.0f) * ((float) dig_P1);
        float p = 1048576.0f - (float) adc_p;
        p = (p - (var2 / 4096.0f)) * 6250.0f / var1;
        var1 = ((float) dig_P9) * p * p / 2147483648.0f;
        var2 = p * ((float) dig_P8) / 32768.0f;
        float pressure = (p + (var1 + var2 + ((float) dig_P7)) / 16.0f) / 100;

        // Humidity offset calculations
        float var_H = (t_fine - 76800.0f);
        var_H = (adc_h - (dig_H4 * 64.0f + dig_H5 / 16384.0f * var_H)) * (dig_H2 / 65536.0f * (1.0f + dig_H6 / 67108864.0f * var_H * (1.0f + dig_H3 / 67108864.0f * var_H)));
        float humidity = var_H * (1.0f - dig_H1 * var_H / 524288.0f);
        if (humidity > 100.0f) {
            humidity = 100.0f;
        } else if (humidity < 0.0f) {
            humidity = 0.0f;
        }
        this.temperature = cTemp; //fTemp
        this.pressure = pressure;
        this.humidity = humidity;
    }

    /**
     * @return the temperature
     */
    public float getTemperature() {
        return this.temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(float themperature) {
        this.temperature = themperature;
    }

    /**
     * @return the humidity
     */
    public float getHumidity() {
        return this.humidity;
    }

    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    /**
     * @return the pressure
     */
    public float getPressure() {
        return this.pressure;
    }

    /**
     * @param pressure the pressure to set
     */
    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public void configure(Config config) {
        this.config = config;
    }

    private int read(int address) throws IOException {
        return config.read(address);
    }

    private void read(int address, byte[] buffer) throws Exception {
        config.read(address, buffer, buffer.length);
    }

    private void read(int address, byte[] buffer, int length) throws Exception {
        config.read(address, buffer, length);
    }

    private void write(int address, byte command) throws IOException {
        config.write(address, command);
    }

    public interface Config {
        int read(int address) throws IOException;
        void read(int address, byte[] buffer, int length) throws Exception;
        void write(int address, byte command) throws IOException;
    }
}
