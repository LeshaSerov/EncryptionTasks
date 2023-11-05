package alex.serov.task4;

import alex.serov.Cipher;
import java.math.BigInteger;
import java.util.Random;

public class RSACipherImpl implements Cipher {

  private final BigInteger N;
  private BigInteger e;
  private final BigInteger d;

  public RSACipherImpl() {
    Random r = new Random();
    int maxLength = 1024;
    BigInteger p = BigInteger.probablePrime(maxLength, r);
    BigInteger q = BigInteger.probablePrime(maxLength, r);
    N = p.multiply(q);
    BigInteger PHI = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
    e = BigInteger.probablePrime(maxLength / 2, r);
    while (PHI.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(PHI) < 0) {
      e = e.add(BigInteger.ONE);
    }
    d = e.modInverse(PHI);
  }

  public RSACipherImpl(BigInteger e, BigInteger d, BigInteger N) {
    this.e = e;
    this.d = d;
    this.N = N;
  }

  private static String bToS(byte[] cipher) {
    StringBuilder temp = new StringBuilder();
    for (byte b : cipher) {
      temp.append(b);
    }
    return temp.toString();
  }

  @Override
  public String encrypt(String message) {
    byte[] messageBytes = message.getBytes();
    BigInteger plaintext = new BigInteger(messageBytes);
    BigInteger ciphertext = plaintext.modPow(e, N);
    return ciphertext.toString();
  }

  @Override
  public String decrypt(String message) {
    BigInteger ciphertext = new BigInteger(message);
    BigInteger plaintext = ciphertext.modPow(d, N);
    byte[] plainBytes = plaintext.toByteArray();
    return new String(plainBytes);
  }

  public static void main(String[] arguments) {
    RSACipherImpl rsaCipherImpl = new RSACipherImpl();
    String message = "Hello world!";
    System.out.println("Encrypting the message: " + message);
    String cipher = rsaCipherImpl.encrypt(message);
    System.out.println("Encrypted message: " + cipher);
    String plain = rsaCipherImpl.decrypt(cipher);
    System.out.println("Decrypted message: " + plain);
  }
}
