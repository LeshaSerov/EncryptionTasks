package alex.serov.task3;

import static alex.serov.BitConversionInf.stringToBinary;

import alex.serov.CipherInf;
import java.math.BigInteger;

public class RC5Cipher implements CipherInf {

  public static final String INVALID_W = "Invalid w. Supported sizes are 16, 32, and 64 bits.";
  public static final String INVALID_R = "Invalid r. Should be between 0 and 255.";
  public static final String INVALID_B = "Invalid b. Should be between 0 and 255 bytes.";
  private int wordSizeBits;  // w - Размер подблоков (16,32,64)
  private int numRounds;  // r - Количество раундов
  private int keyLengthBytes; // b - Длина ключа в байтах
  private int numRoundKeys;  // t - Количество раундовых ключей (связей) 2 * (r + 1)
  private ExpandedBigInteger P; // P - константа для заполнения массива подключей
  private ExpandedBigInteger Q; // Q - константа для заполнения массива подключей
  ExpandedBigInteger[] roundKeys; // S[0.. 2*r + 1] - массив ключей

  private RC5Cipher() {
  }

  public static RC5Cipher createInstance(int w, int r, String key) {
    RC5Cipher cipher = new RC5Cipher();
    cipher.validateParameters(w, r, key);
    cipher.wordSizeBits = w;
    cipher.numRounds = r;
    cipher.keyLengthBytes = key.length();
    cipher.numRoundKeys = 2 * (r + 1);
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
    switch (wordSizeBits) {
      case 16 -> {
        P = new ExpandedBigInteger("b7e1", 16, wordSizeBits);
        Q = new ExpandedBigInteger("9e37", 16, wordSizeBits);
      }
      case 32 -> {
        P = new ExpandedBigInteger("b7e15163", 16, wordSizeBits);
        Q = new ExpandedBigInteger("9e3779b9", 16, wordSizeBits);
      }
      case 64 -> {
        P = new ExpandedBigInteger("b7e151628aed2a6b", 16, wordSizeBits);
        Q = new ExpandedBigInteger("9e3779b97f4a7c15", 16, wordSizeBits);
      }
    }
  }

  private void initializeRoundKeys(String key) {
    // Создаем массив байт ключа
    byte[] secretKey = key.getBytes();
    // Рассчитываем количество байтов в слове.
    int bytesPerWord = wordSizeBits / 8;
    // Рассчитываем количество слов в массиве L.
    int numWordsInL = keyLengthBytes / bytesPerWord;
    // Инициализируем массив ExpandedBigInteger L нулевыми значениями.
    ExpandedBigInteger[] L = new ExpandedBigInteger[numWordsInL];
    for (int wordIndex = 0; wordIndex < numWordsInL; wordIndex++) {
      L[wordIndex] = new ExpandedBigInteger(BigInteger.ZERO, wordSizeBits);
    }
    // Проходим по байтам секретного ключа в обратном порядке.
    for (int byteIndex = keyLengthBytes - 1; byteIndex >= 0; byteIndex--) {
      // Вычисляем индекс в массиве L (L[wordIndex]), где wordIndex - индекс слова в массиве.
      int wordIndex = byteIndex / bytesPerWord;
      // Сдвигаем содержимое массива L на 8 битов влево и добавляем к текущему значению L.
      L[wordIndex] = L[wordIndex].cyclicLeftShift(8)
          .xor(new ExpandedBigInteger(BigInteger.valueOf(secretKey[byteIndex] & 0xFF), wordSizeBits));
    }

    // Создаем массив раундовых ключей размером numRoundKeys.
    roundKeys = new ExpandedBigInteger[numRoundKeys];
    // Инициализируем первый раундовый ключ значением P.
    roundKeys[0] = P;
    // Генерируем остальные раундовые ключи, начиная с первого и идя до numRoundKeys - 1.
    for (int roundKeyIndex = 1; roundKeyIndex < numRoundKeys; roundKeyIndex++) {
      // Каждый следующий раундовый ключ вычисляется путем прибавления Q к предыдущему ключу.
      roundKeys[roundKeyIndex] = roundKeys[roundKeyIndex - 1].add(Q);
    }

    // Инициализируем объекты ExpandedBigInteger A и B нулевыми значениями.
    ExpandedBigInteger A = new ExpandedBigInteger(BigInteger.ZERO, wordSizeBits);
    ExpandedBigInteger B = new ExpandedBigInteger(BigInteger.ZERO, wordSizeBits);
    // Инициализируем переменные для навигации по массивам roundKeys и L.
    int i = 0;
    int j = 0;
    // Выполняем операции над A и B в цикле.
    for (int k = 0; k < 3 * Math.max(numRoundKeys, numWordsInL); k++) {
      // Для A: Вычисляем новое значение и присваиваем его элементу массива roundKeys с индексом roundKeyPointer.
      A = roundKeys[i] = roundKeys[i].add(A).add(B).cyclicLeftShift(3);
      // Для B: Вычисляем новое значение и присваиваем его элементу массива L с индексом wordPointer.
      B = L[j] = L[j].add(A).add(B).cyclicLeftShift(A.add(B));
      // Обновляем индексы roundKeyPointer и wordPointer, чтобы двигаться по массивам roundKeys и L.
      i = (i + 1) % numRoundKeys;
      j = (j + 1) % numWordsInL;
    }
  }


  private String encryptBlock(String block) {
    ExpandedBigInteger A = new ExpandedBigInteger(block.substring(0, block.length() / 2), 2, wordSizeBits);
    ExpandedBigInteger B = new ExpandedBigInteger(block.substring(block.length() / 2), 2, wordSizeBits);
    A = A.add(roundKeys[0]);
    B = B.add(roundKeys[1]);
    for (int i = 1; i <= numRounds; i++) {
      A = A.xor(B).cyclicLeftShift(B).add(roundKeys[2 * i]);
      B = B.xor(A).cyclicLeftShift(A).add(roundKeys[2 * i + 1]);
    }
    return A.toString(16) + B.toString(16);
  }

  private String decryptBlock(String block) {
    ExpandedBigInteger A = new ExpandedBigInteger(block.substring(0, block.length() / 2), 2, wordSizeBits);
    ExpandedBigInteger B = new ExpandedBigInteger(block.substring(block.length() / 2), 2, wordSizeBits);

    for (int i = numRounds; i >= 1; i--) {
      B = B.subtract(roundKeys[2 * i + 1]).cyclicRightShift(A).xor(A);
      A = A.subtract(roundKeys[2 * i]).cyclicRightShift(B).xor(B);
    }
    B = B.subtract(roundKeys[1]);
    A = A.subtract(roundKeys[0]);

    return A.toString(16) + B.toString(16);
  }


  @Override
  public String encrypt(String message) {
    // Преобразование строки в битовую строку
    String binaryMessage = stringToBinary(message);
    StringBuilder encryptedMessage = new StringBuilder();
    // Разбиваем битовую строку на блоки размером w
    for (int i = 0; i < binaryMessage.length(); i += 2 * wordSizeBits) {
      StringBuilder block = new StringBuilder(
          binaryMessage.substring(i, Math.min(i + 2 * wordSizeBits, binaryMessage.length())));
      // Дополняем блок, если необходимо
      toFillDoNecessarySize(block);
      String encryptBlock = encryptBlock(block.toString());
      encryptedMessage.append(encryptBlock);
    }
    return encryptedMessage.toString();
  }

  @Override
  public String decrypt(String message) {
    // Преобразование строки в битовую строку
    String binaryMessage = stringToBinary(message);
    StringBuilder decryptedMessage = new StringBuilder();
    // Разбиваем битовую строку на блоки размером w
    for (int i = 0; i < binaryMessage.length(); i += 2 * wordSizeBits) {
      StringBuilder block = new StringBuilder(
          binaryMessage.substring(i, Math.min(i + 2 * wordSizeBits, binaryMessage.length())));
      toFillDoNecessarySize(block);
      String decryptBlock = decryptBlock(block.toString());
      decryptedMessage.append(decryptBlock);
    }
    return decryptedMessage.toString();
  }

  private void toFillDoNecessarySize(StringBuilder block) {
    // Дополняем блок, если необходимо
    int blockSize = 2 * wordSizeBits;
    int missingBits = blockSize - block.length();
    if (missingBits > 0) {
      block.append("1");  // добавляем бит "1"
      block.append("0".repeat(missingBits - 1));  // добавляем биты "0"
    }
  }


  public static void main(String[] args) {
    String message = "Hi, I am Alex!";
    String key = "a";
    CipherInf cipher = RC5Cipher.createInstance(8*key.length(), 250, key);
    String encryptMessage = cipher.encrypt(message);
    System.out.println(encryptMessage);
    String decryptMessage = cipher.decrypt(encryptMessage);
    System.out.println(decryptMessage);
  }

}
