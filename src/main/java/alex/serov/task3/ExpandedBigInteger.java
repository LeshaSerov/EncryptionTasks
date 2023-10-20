package alex.serov.task3;

import java.math.BigInteger;

public class ExpandedBigInteger {

  private final BigInteger element;
  private final int size;

  public ExpandedBigInteger(String val, int radix, int wordSizeBits) {
    element = new BigInteger(val, radix);
    size = wordSizeBits;
  }

  public ExpandedBigInteger(BigInteger bigInteger, int wordSizeBits) {
    element = bigInteger;
    size = wordSizeBits;
  }

  public String toString(int radix) {
    return element.toString(radix);
  }

  public ExpandedBigInteger cyclicLeftShift(int y) {
    y = y % size;
    return new ExpandedBigInteger(
        this.element.shiftLeft(y).or(this.element.shiftRight(size - y))
            .and(BigInteger.TWO.pow(size).subtract(BigInteger.ONE)), size);
  }

  public ExpandedBigInteger cyclicLeftShift(ExpandedBigInteger elementY) {
    int y = elementY.element.intValue();
    y = y % size;
    return new ExpandedBigInteger(
        this.element.shiftLeft(y).or(this.element.shiftRight(size - y))
            .and(BigInteger.TWO.pow(size).subtract(BigInteger.ONE)), size);
  }

  public ExpandedBigInteger cyclicRightShift(int y) {
    y = y % size;
    return new ExpandedBigInteger(
        this.element.shiftRight(y).or(this.element.shiftLeft(size - y))
            .and(BigInteger.TWO.pow(size).subtract(BigInteger.ONE)), size);
  }

  public ExpandedBigInteger cyclicRightShift(ExpandedBigInteger elementY) {
    int y = elementY.element.intValue();
    y = y % size;
    return new ExpandedBigInteger(
        this.element.shiftRight(y).or(this.element.shiftLeft(size - y))
            .and(BigInteger.TWO.pow(size).subtract(BigInteger.ONE)), size);
  }

  public ExpandedBigInteger add(ExpandedBigInteger b) {
    BigInteger modulo = BigInteger.TWO.pow(size);
    return new ExpandedBigInteger(this.element.add(b.element).mod(modulo), size);
  }

  public ExpandedBigInteger subtract(ExpandedBigInteger b) {
    BigInteger modulo = BigInteger.TWO.pow(size);
    return new ExpandedBigInteger(this.element.subtract(b.element).mod(modulo), size);
  }

  public ExpandedBigInteger xor(ExpandedBigInteger b) {
    BigInteger result = this.element.xor(b.element);
    return new ExpandedBigInteger(result, size);
  }

}
