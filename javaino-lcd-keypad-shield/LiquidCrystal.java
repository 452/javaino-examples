import hw.Logger;
import hw.IO;
import hw.IOException;
import hw.Console;
import hw.Time;
import java.util.List;
import java.util.ArrayList;

/**
 * Liquid crystal driver
 * need to improve and fix bugs, 8bit mode not work
 */
public class LiquidCrystal {

    private static final String TAG = "LiquidCrystal library";

    // commands
    private static final byte LCD_CLEARDISPLAY = 0x01;
    private static final byte LCD_RETURNHOME = 0x02;
    private static final byte LCD_ENTRYMODESET = 0x04;
    private static final byte LCD_DISPLAYCONTROL = 0x08;
    private static final byte LCD_CURSORSHIFT = 0x10;
    private static final byte LCD_FUNCTIONSET = 0x20;
    private static final byte LCD_SETCGRAMADDR = 0x40;
    private static final short LCD_SETDDRAMADDR = 0x80;

    // flags for display entry mode
    private static final byte LCD_ENTRYRIGHT = 0x00;
    private static final byte LCD_ENTRYLEFT = 0x02;
    private static final byte LCD_ENTRYSHIFTINCREMENT = 0x01;
    private static final byte LCD_ENTRYSHIFTDECREMENT = 0x00;

    // flags for display on/off control
    private static final byte LCD_DISPLAYON = 0x04;
    private static final byte LCD_DISPLAYOFF = 0x00;
    private static final byte LCD_CURSORON = 0x02;
    private static final byte LCD_CURSOROFF = 0x00;
    private static final byte LCD_BLINKON = 0x01;
    private static final byte LCD_BLINKOFF = 0x00;

    // flags for display/cursor shift
    private static final byte LCD_DISPLAYMOVE = 0x08;
    private static final byte LCD_CURSORMOVE = 0x00;
    private static final byte LCD_MOVERIGHT = 0x04;
    private static final byte LCD_MOVELEFT = 0x00;

    // flags for function set
    private static final byte LCD_8BITMODE = 0x10;
    private static final byte LCD_4BITMODE = 0x00;
    private static final byte LCD_2LINE = 0x08;
    private static final byte LCD_1LINE = 0x00;
    private static final byte LCD_5x10DOTS = 0x04;
    private static final byte LCD_5x8DOTS = 0x00;


    private boolean backlight; // use backlight

    private byte displayFunction; // lines and dots mode
    private byte displayControl; // cursor, display, blink flags
    private byte displayMode; // left2right, autoscroll

    private int numLines; // The number of rows the display supports.

    private byte enablePin = 9;
    private byte resetPin = 8;

    private static final byte RSMODE_CMD = 0;
    private static final byte RSMODE_DATA = 1;

    boolean lcdBitMode = true; // true 4 bit, false 8 bit

    private ArrayList dataBus = new ArrayList(8);

    public LiquidCrystal() throws IOException {
        begin(16, 2);
    }

    public void begin(int cols, int rows) throws IOException {
        begin(cols, rows, LCD_5x8DOTS);
    }

    public void begin(int cols, int rows, int charsize) throws IOException {

        if (!lcdBitMode) {
            Integer d1 = new Integer(1);
            Integer d2 = new Integer(2);
            Integer d3 = new Integer(3);
            dataBus.add(d1);
            dataBus.add(d2);
            dataBus.add(d3);
        }
        Integer d4 = new Integer(4);
        Integer d5 = new Integer(5);
        Integer d6 = new Integer(6);
        Integer d7 = new Integer(7);
        dataBus.add(d4);
        dataBus.add(d5);
        dataBus.add(d6);
        dataBus.add(d7);

        if (lcdBitMode) {
            dataBus.trimToSize();
            displayFunction = LCD_4BITMODE | LCD_1LINE | LCD_5x8DOTS;
        } else {
            displayFunction = LCD_8BITMODE | LCD_1LINE | LCD_5x8DOTS;
        }

        // cols ignored !
        numLines = rows;

        //  displayFunction = 0;

        if (rows > 1) {
            displayFunction |= LCD_2LINE;
        }

        // for some 1 line displays you can select a 10 pixel high font
        if ((charsize != 0) && (rows == 1)) {
            displayFunction |= LCD_5x10DOTS;
        }

        // HD44780
        // SEE PAGE 45/46 FOR INITIALIZATION SPECIFICATION!
        // according to datasheet, we need at least 40ms after power rises above 2.7V
        // before sending commands.

        for (int i = 0; i < dataBus.size(); ++i) {
            IO.setOutput(((Integer) dataBus.get(i)).intValue(), 1);
        }

        delay(50000);
        IO.setOutput(resetPin, IO.OUTPUT_SET_TO_OFF);
        IO.setOutput(enablePin, IO.OUTPUT_SET_TO_OFF);

        if (lcdBitMode) {
            write4bits((byte) 0x03);
            delay(4500);
            write4bits((byte) 0x03);
            delay(4500);
            write4bits((byte) 0x03);
            delay(150);
            write4bits((byte) 0x02);
        } else {
            // this is according to the hitachi HD44780 datasheet
            // page 45 figure 23

            // Send function set command sequence
            command(LCD_FUNCTIONSET | displayFunction);
            delay(4500); // wait more than 4.1ms

            // second try
            command(LCD_FUNCTIONSET | displayFunction);
            delay(150);

            // third go
            command(LCD_FUNCTIONSET | displayFunction);
        }
        // finally, set # lines, font size, etc.
        command(LCD_FUNCTIONSET | displayFunction);

        // turn the display on with no cursor or blinking default
        displayControl = LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF;
        display();

        // clear it off
        clear();

        // Initialize to default text direction (for romance languages)
        displayMode = LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT;
        // set the entry mode
        command(LCD_ENTRYMODESET | displayMode);
    }

    public void clear() {
        command(LCD_CLEARDISPLAY); // clear display, set cursor position to zero
        delay(2000); // this command takes a long time!
    }

    public void home() {
        command(LCD_RETURNHOME); // set cursor position to zero
        delay(4);
        delay(2000); // this command takes a long time!
    }

    // Set the cursor to a new position.
    public void setCursor(int col, int row) {
        int row_offsets[] = {
            0x00,
            0x40,
            0x14,
            0x54
        };
        if (row >= numLines) {
            row = numLines - 1; // we count rows starting w/0
        }
        command(LCD_SETDDRAMADDR | (col + row_offsets[row]));
    }

    // Turn the display on/off (quickly)
    public void noDisplay() {
        displayControl &= ~LCD_DISPLAYON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    public void display() {
        displayControl |= LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    // Turn on and off the blinking cursor
    public void noBlink() {
        displayControl &= ~LCD_BLINKON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    public void blink() {
        displayControl |= LCD_BLINKON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    // Turns the underline cursor on/off
    public void noCursor() {
        displayControl &= ~LCD_CURSORON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    public void cursor() {
        displayControl |= LCD_CURSORON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    // These commands scroll the display without changing the RAM
    public void scrollDisplayLeft() {
        command(LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVELEFT);
    }

    public void scrollDisplayRight() {
        command(LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVERIGHT);
    }

    // This is for text that flows Left to Right
    public void leftToRight() {
        displayMode |= LCD_ENTRYLEFT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    // This is for text that flows Right to Left
    public void rightToLeft() {
        displayMode &= ~LCD_ENTRYLEFT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    // This will 'right justify' text from the cursor
    public void autoscroll() {
        displayMode |= LCD_ENTRYSHIFTINCREMENT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    // This will 'left justify' text from the cursor
    public void noAutoscroll() {
        displayMode &= ~LCD_ENTRYSHIFTINCREMENT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    public void print(String message) {
        try {
            IO.setOutput(resetPin, RSMODE_DATA);
            for (int i = 0; i < message.length(); i++) {
                send(message.charAt(i));
            }
        } catch (Exception e) {
            Logger.e(TAG + " " + e.getMessage());
        }
    }

    private void command(int value) {
        try {
            IO.setOutput(resetPin, RSMODE_CMD);
            send(value);
        } catch (Exception e) {
            Logger.e(TAG + " " + e.getMessage());
        }
    }

    // write either command or data
    private void send(int value) {
        try {
            //if ((displayFunction & LCD_4BITMODE) == 0) { // improve me
            //if (lcdBitMode) {
                write4bits(value);
            //} else {
            //    write8bits(value);
            //}
        } catch (Exception e) {
            Logger.e(TAG + " " + e.getMessage());
        }
    }

    private void write4bits(int value) throws IOException {
        write(value >> 4);
        write(value);
    }

    private void write8bits(int value) throws IOException {
        write(value);
    }

    private void write(int value) throws IOException {
        for (int i = 0; i < dataBus.size(); i++) {
            IO.setOutput(((Integer) dataBus.get(i)).intValue(), (value >> i) & 0x01);
        }
        pulseEnable();
    }

    private void pulseEnable() throws IOException {
        IO.setOutput(enablePin, IO.OUTPUT_SET_TO_OFF);
        delay(1);
        IO.setOutput(enablePin, IO.OUTPUT_SET_TO_ON);
        delay(1);
        IO.setOutput(enablePin, IO.OUTPUT_SET_TO_OFF);
        delay(100);
    }

    private void delay(int microseconds) {
        Time.delay(Math.max(1, Math.round((float) 0.001 * microseconds)));
    }
}
