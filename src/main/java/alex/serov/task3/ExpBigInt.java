package alex.serov.task3;

import alex.serov.BitConversion;
import java.math.BigInteger;
import java.util.Arrays;

public class ExpBigInt {

  private final BigInteger element;
  private final int size;
  private final BigInteger modulo;

  public ExpBigInt(String val, int radix, int wordSizeBits) {
    element = new BigInteger(val, radix);
    size = wordSizeBits;
    modulo = BigInteger.TWO.pow(size);
  }

  public ExpBigInt(BigInteger bigInteger, int wordSizeBits) {
    element = bigInteger;
    size = wordSizeBits;
    modulo = BigInteger.TWO.pow(size);
  }

  public BigInteger getElement() {
    return element;
  }

  public String toString(int radix) {
    String result = element.toString(radix);
    return BitConversion.getRepeat(result, size) + result;
  }

  private char[] padCharArray() {
    String binaryString = element.toString(2);
    if (binaryString.length() == size) {
      return binaryString.toCharArray();
    }
    char[] result = new char[size];
    Arrays.fill(result, '0');

    int resultIndex = size - binaryString.length();
    for (char bit : binaryString.toCharArray()) {
      result[resultIndex++] = bit;
    }
    return result;
  }

  public ExpBigInt cycleShift(int n) {
    char[] chars = padCharArray();
    n = n % size; // Обработка смещения, учитывая размер массива
    if (n < 0) {
      n = size + n; // Обработка отрицательного смещения
    }
    char[] result = new char[size];
    for (int i = 0; i < size; i++) {
      int newPosition = (i - n + size) % size;
      result[newPosition] = chars[i];
    }
    StringBuilder binaryStr = new StringBuilder();
    for (char bit : result) {
      binaryStr.append(bit);
    }
    BigInteger shiftedElement = new BigInteger(binaryStr.toString(), 2);
    return new ExpBigInt(shiftedElement, size);
  }

  public ExpBigInt cycleShiftLeft(int n) {
    return cycleShift(n);
  }

  public ExpBigInt cycleShiftLeft(ExpBigInt b) {
    return cycleShift(b.element.intValue());
  }

  public ExpBigInt cycleShiftRight(ExpBigInt b) {
    return cycleShift(-b.element.intValue());
  }

  public ExpBigInt add(BigInteger b) {
    return new ExpBigInt(
        element.add(b).mod(modulo),
        size
    );
  }

  public ExpBigInt add(ExpBigInt b) {
    return add(b.element);
  }

  public ExpBigInt subtract(ExpBigInt b) {
    return new ExpBigInt(
        element.subtract(b.element).mod(modulo),
        size
    );
  }

  public ExpBigInt xor(ExpBigInt b) {
    return new ExpBigInt(
        element.xor(b.element),
        size
    );
  }

}
