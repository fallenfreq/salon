package salon;

import java.text.NumberFormat;
import java.util.Locale;

// A utility class to hold generic utility methods that don't fit in a specific class.
final class MyUtilities {
  private MyUtilities() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // Generates a random number between min and max
  public static int randomInteger(int min, int max) {
    return (int) (Math.random() * ((max - min) + 1)) + min;
  }

  // Formats a number like 10 that represents £10,000 to be just that by passing the number to
  // format and the times amount
  static String displayInPounds(Number amount, int times) {
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK);
    currencyFormat.setMinimumFractionDigits(0);
    return currencyFormat.format(amount.doubleValue() * times);
  }
}
