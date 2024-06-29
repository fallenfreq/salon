package salon;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

class ValidationException extends RuntimeException {
  private static final long serialVersionUID = -7999489834309874493L;

  public ValidationException(String message) {
    super(message);
  }
}


public class Validator {

  // Validator for comma-separated values with an additional validator
  public static <V> V[] validateCommaSeparatedString(
    String input, Function<String, V> itemValidator) {
    String[] items = input.split(",");
    @SuppressWarnings("unchecked")
    V[] results = (V[]) new Object[items.length];
    for (int i = 0; i < items.length; i++) {
      String trimmedItem = items[i].trim();
      V result = itemValidator.apply(trimmedItem);
      if (result == null) {
        return null;
      }
      results[i] = result;
    }
    return results;
  }

  // Validator for checking if a string represents a number within a specified range
  public static Integer isValidNumberInRange(String input, int min, int max) {
    try {
      int number = Integer.parseInt(input);
      return number >= min && number <= max ? number : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  // Validator for checking string length bounds
  public static String isValidStringLength(String input, int minLength, int maxLength) {
    return input.length() >= minLength && input.length() <= maxLength ? input : null;
  }

  // Validator for checking a valid phone number using regex
  public static String isValidPhoneNumber(String phoneNumber) {
    return Pattern.compile("^[\\d\\s-()+]{7,20}$").matcher(phoneNumber).matches()
      && phoneNumber.chars().filter(Character::isDigit).count() >= 7
        ? phoneNumber
        : null;
  }

  // Validator for checking if a string matches one of several allowed values
  public static <V> V validateAllowedValues(V input, Set<V> allowedValues) {
    return allowedValues.contains(input) ? input : null;
  }
}
