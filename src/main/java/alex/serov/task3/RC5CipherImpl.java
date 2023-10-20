package alex.serov.task3;

import static alex.serov.BitConversion.stringToBinary;

import alex.serov.BitConversion;
import alex.serov.Cipher;
import java.math.BigInteger;

public class RC5CipherImpl implements Cipher {

  public static final String INVALID_W = "Invalid w. Supported sizes are 16, 32, and 64 bits.";
  public static final String INVALID_R = "Invalid r. Should be between 0 and 255.";
  public static final String INVALID_B = "Invalid b. Should be between 0 and 255 bytes.";
  private int subBlockSizeBits;  // w - Размер подблоков (16, 32, 64)
  private int numRounds;  // r - Количество раундов
  private int keyLengthBytes; // b - Длина ключа в байтах
  //  private int numRoundKeys;  // t - Количество раундовых ключей (связей) 2 * (r + 1)
  private ExpBigInt P; // P - константа для заполнения массива подключей
  private ExpBigInt Q; // Q - константа для заполнения массива подключей
  ExpBigInt[] roundKeys; // S[0.. 2*r + 1] - массив ключей

  private RC5CipherImpl() {
  }

  public static RC5CipherImpl createInstance(int w, int r, String key) {
    RC5CipherImpl cipher = new RC5CipherImpl();
    cipher.validateParameters(w, r, key);
    cipher.subBlockSizeBits = w;
    cipher.numRounds = r;
    cipher.keyLengthBytes = key.length();
//    cipher.numRoundKeys = 2 * (r + 1);
    cipher.initializeConstPAndQ();
    cipher.initializeRoundKeys(key);
    return cipher;
  }

  private void validateParameters(int w, int r, String key) {
    if (w != 16 && w != 32 && w != 64) {
      throw new IllegalArgumentException(INVALID_W);
    }
    if (r < 0 || r > 255) {
      throw new IllegalArgumentException(INVALID_R);
    }
    if (key == null || key.length() == 0 || key.length() > 255) {
      throw new IllegalArgumentException(INVALID_B);
    }
  }

  private void initializeConstPAndQ() {
    switch (subBlockSizeBits) {
      case 16 -> {
        P = new ExpBigInt("b7e1", 16, subBlockSizeBits);
        Q = new ExpBigInt("9e37", 16, subBlockSizeBits);
      }
      case 32 -> {
        P = new ExpBigInt("b7e15163", 16, subBlockSizeBits);
        Q = new ExpBigInt("9e3779b9", 16, subBlockSizeBits);
      }
      case 64 -> {
        P = new ExpBigInt("b7e151628aed2a6b", 16, subBlockSizeBits);
        Q = new ExpBigInt("9e3779b97f4a7c15", 16, subBlockSizeBits);
      }
    }
  }

  private void initializeRoundKeys(String key) {
    byte[] secretKey = key.getBytes();
    // Рассчитываем количество байтов в слове.
    int u = subBlockSizeBits / 8;
    // Рассчитываем количество слов в массиве L.
    int c = keyLengthBytes % u > 0 ?
        keyLengthBytes / u + 1 : keyLengthBytes / u;
    // Инициализируем массив ExpandedBigInteger L нулевыми значениями.
    ExpBigInt[] L = new ExpBigInt[c];
    for (int wordIndex = 0; wordIndex < c; wordIndex++) {
      L[wordIndex] = new ExpBigInt(BigInteger.ZERO, subBlockSizeBits);
    }
    // Проходим по байтам секретного ключа в обратном порядке.
    for (int i = keyLengthBytes - 1; i >= 0; i--) {
      // Сдвигаем содержимое массива L на 8 битов влево и добавляем к текущему значению L часть ключа.
      ExpBigInt elementKey = new ExpBigInt(
          BigInteger.valueOf(secretKey[i] & 0xFF), subBlockSizeBits
      );
      L[i / u] = L[i / u].cycleShiftLeft(8).add(elementKey);
    }

    int numRoundKeys = 2 * (numRounds + 1);
    // Создаем массив раундовых ключей размером numRoundKeys.
    roundKeys = new ExpBigInt[numRoundKeys];
    // Инициализируем первый раундовый ключ значением P.
    roundKeys[0] = P;
    // Генерируем остальные раундовые ключи, начиная с первого и идя до numRoundKeys - 1.
    for (int roundKeyIndex = 1; roundKeyIndex < numRoundKeys; roundKeyIndex++) {
      // Каждый следующий раундовый ключ вычисляется путем прибавления Q к предыдущему ключу.
      roundKeys[roundKeyIndex] = roundKeys[roundKeyIndex - 1].add(Q);
    }

    // Инициализируем объекты ExpandedBigInteger A и B нулевыми значениями.
    ExpBigInt A = new ExpBigInt(BigInteger.ZERO, subBlockSizeBits);
    ExpBigInt B = new ExpBigInt(BigInteger.ZERO, subBlockSizeBits);
    // Инициализируем переменные для навигации по массивам roundKeys и L.
    int i = 0;
    int j = 0;
    // Выполняем операции над A и B в цикле.
    int max = 3 * Math.max(numRoundKeys, c);
    for (int k = 0; k < max; k++) {
      // Для A: Вычисляем новое значение и присваиваем его элементу массива roundKeys с индексом roundKeyPointer.
      A = roundKeys[i] = roundKeys[i].add(A).add(B).cycleShiftLeft(3);
      // Для B: Вычисляем новое значение и присваиваем его элементу массива L с индексом wordPointer.
      B = L[j] = L[j].add(A).add(B).cycleShiftLeft(A.add(B).getElement().intValue());
      // Обновляем индексы roundKeyPointer и wordPointer, чтобы двигаться по массивам roundKeys и L.
      i = (i + 1) % numRoundKeys;
      j = (j + 1) % c;
    }
  }


  private String encryptBlock(String block) {
    String[] parts = getHalves(block);

    ExpBigInt A = new ExpBigInt(parts[0], 2, subBlockSizeBits);
    ExpBigInt B = new ExpBigInt(parts[1], 2, subBlockSizeBits);
    if (numRounds > 0) {
      A = A.add(roundKeys[0]);
      B = B.add(roundKeys[1]);
    }
    for (int i = 1; i < numRounds + 1; i++) {
      A = A.xor(B).cycleShiftLeft(B).add(roundKeys[2 * i]);
      B = B.xor(A).cycleShiftLeft(A).add(roundKeys[2 * i + 1]);
    }
    return A.toString(2) + B.toString(2);
  }

  private String decryptBlock(String block) {
    String[] parts = getHalves(block);

    ExpBigInt A = new ExpBigInt(parts[0], 2, subBlockSizeBits);
    ExpBigInt B = new ExpBigInt(parts[1], 2, subBlockSizeBits);

    for (int i = numRounds; i > 0; i--) {
      B = B.subtract(roundKeys[2 * i + 1])
          .cycleShiftRight(A)
          .xor(A);
      A = A.subtract(roundKeys[2 * i])
          .cycleShiftRight(B)
          .xor(B);
    }
    if (numRounds > 0) {
      B = B.subtract(roundKeys[1]);
      A = A.subtract(roundKeys[0]);
    }

    return A.toString(2) + B.toString(2);
  }

  private static String[] getHalves(String block) {
    String[] parts = new String[2];
    int middle = block.length() / 2;
    parts[0] = block.substring(0, middle);
    parts[1] = block.substring(middle);
    return parts;
  }

  private String crypt(String message, boolean isEncrypt) {
    // Преобразование строки в битовую строку
    String binaryMessage = stringToBinary(message);
      StringBuilder cryptedMessage = new StringBuilder();
    // Разбиваем битовую строку на блоки размером w
    for (int i = 0; i < binaryMessage.length(); i += 2 * subBlockSizeBits) {
      int min = Math.min(i + 2 * subBlockSizeBits, binaryMessage.length());
      String substring = binaryMessage.substring(i, min);
      substring = substring + BitConversion.getRepeat(substring, 2 * subBlockSizeBits);
      String cryptBlock;
      if (isEncrypt) {
        cryptBlock = decryptBlock(substring);
      } else {
        cryptBlock = encryptBlock(substring);
      }
      cryptedMessage.append(cryptBlock);
    }
    return BitConversion.binaryToString(cryptedMessage.toString());
  }


  @Override
  public String encrypt(String message) {
    return crypt(message, false);
  }

  @Override
  public String decrypt(String message) {
    return crypt(message, true);
  }


  public static void main(String[] args) {
    String message = "Hi, I am Alex)) I've been doing this job for eight hours, have mercy.";
    String key = "Key";
    Cipher cipher = RC5CipherImpl.createInstance(16, 250, key);
    String encryptMessage = cipher.encrypt(message);
    System.out.println(encryptMessage);
    String decryptMessage = cipher.decrypt(encryptMessage);
    System.out.println(decryptMessage);
  }

}
