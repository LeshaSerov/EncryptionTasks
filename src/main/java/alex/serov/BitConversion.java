package alex.serov;

public interface BitConversion {

  static String getRepeat(String result, int size) {
    return "0".repeat(Math.max(0, size - result.length()));
  }

  static String stringToBinary(String input) {
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      String binaryChar = Integer.toBinaryString(c);
      result.append("0".repeat(8 - binaryChar.length())).append(binaryChar);
    }
    return result.toString();
  }

  static String binaryToString(String binary) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < binary.length(); i += 8) {
      String binaryChar = binary.substring(i, Math.min(i + 8, binary.length()));
      char charValue = (char) Integer.parseInt(binaryChar, 2);
      result.append(charValue);
    }
    return result.toString();
  }

}
