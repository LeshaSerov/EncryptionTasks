package alex.serov.task2;

import alex.serov.CipherInf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация шифра ADFGVX на основе интерфейса Cipher. Этот шифр использует матрицу 6x6 с символами
 * A, D, F, G, V, X, а также процессы шифрования и дешифрования с использованием этой матрицы и
 * ключа.
 */
public class AdfgvxCipher implements CipherInf {

  private static final int ROWS = 6;
  private static final int COLUMNS = 6;
  private final Character[][] cryptoMatrix;
  private static final Character[] matrixSymbols = new Character[]{'A', 'D', 'F', 'G', 'V', 'X'};
  private final String key;

  /**
   * Конструктор, инициализирующий объект AdfgvxCipher с использованием строки шифра.
   * Строка шифра используется для заполнения матрицы 6x6.
   *
   * @param cipherString строка, представляющая матрицу шифра.
   * @throws IllegalArgumentException если длина строки шифра не равна 36 (6x6).
   */
  public AdfgvxCipher(String cipherString, String key) {
    if (cipherString.length() != ROWS * COLUMNS) {
      throw new IllegalArgumentException("Длина строки шифра должна быть равна 36 (6x6).");
    }
    int count = 0;
    this.cryptoMatrix = new Character[ROWS][COLUMNS];
    for (Character c : cipherString.toCharArray()) {
      this.cryptoMatrix[count / (ROWS)][count % (COLUMNS)] = c;
      count++;
    }
    this.key = key;
  }

  /**
   * Метод для шифрования отдельного символа ADFGVX.
   *
   * @param c символ для шифрования.
   * @return зашифрованное представление символа.
   */
  private String encryptSymbol(Character c) {
    for (int i = 0; i < ROWS; i++) {
      for (int j = 0; j < COLUMNS; j++) {
        if (this.cryptoMatrix[i][j] == c) {
          return "" + matrixSymbols[i] + matrixSymbols[j];
        }
      }
    }
    return String.valueOf(c);
  }

  /**
   * Метод для дешифрования двухсимвольного символа ADFGVX.
   * В случае если этот символ представлен единичным символом, возвращает его же
   *
   * @param s двухсимвольный символ для дешифрования.
   * @return дешифрованное представление символа.
   */
  private String decryptSymbol(String s) {
    if (s.length() > 1) {
      int pozX = findIndexMatrixSymbol(s.charAt(0));
      int pozY = findIndexMatrixSymbol(s.charAt(1));
      return String.valueOf(cryptoMatrix[pozX][pozY]);
    }
    return s;
  }

  /**
   * Метод для поиска индекса символа в массиве matrixSymbols.
   *
   * @param symbol символ для поиска.
   * @return индекс символа.
   * @throws IllegalArgumentException если символ не найден.
   */
  private static int findIndexMatrixSymbol(char symbol) {
    for (int i = 0; i < matrixSymbols.length; i++) {
      if (symbol == matrixSymbols[i]) {
        return i;
      }
    }
    throw new IllegalArgumentException("Символ не найден");
  }

  @Override
  public String encrypt(String message) {
    StringBuilder encryptedMessage = message.toUpperCase().chars()
        .mapToObj(c -> encryptSymbol((char) c))
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

    int numRows = (int) Math.round((double) encryptedMessage.toString().length() / key.length());
    char[][] matrix = new char[key.length()][numRows];
    int index = 0;
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < key.length(); j++) {
        if (index < encryptedMessage.toString().length()) {
          matrix[j][i] = encryptedMessage.toString().charAt(index);
        } else {
          matrix[j][i] = '\0'; // символ null
        }
        index++;
      }
    }

    Map<Character, char[]> map = new LinkedHashMap<>();
    for (int i = 0; i < key.length(); i++) {
      map.put(key.charAt(i), matrix[i]);
    }

    StringBuilder finishMessage = new StringBuilder();
    map.entrySet().stream()
        .sorted(Comparator.comparingInt(e -> (int) e.getKey()))
        .forEach(entry -> finishMessage.append(entry.getValue()));
    return finishMessage.toString();
  }

  @Override
  public String decrypt(String message) {
    int numRows = (int) Math.round((double) message.length() / key.length());
    char[][] matrix = new char[key.length()][numRows];
    int index = 0;

    for (int i = 0; i < key.length(); i++) {
      for (int j = 0; j < numRows; j++) {
        if (index < message.length()) {
          matrix[i][j] = message.charAt(index);
        } else {
          matrix[i][j] = '\0'; // null character
        }
        index++;
      }
    }

    char[] keySortArray = key.toCharArray();
    Arrays.sort(keySortArray);
    String keySort = new String(keySortArray);

    Map<Character, char[]> tempMap = new LinkedHashMap<>();
    for (int i = 0; i < keySort.length(); i++) {
      tempMap.put(keySort.charAt(i), matrix[i]);
    }

    List<char[]> sortPairs = new ArrayList<>();
    for (Character c : key.toCharArray()) {
      char[] pair = tempMap.get(c);
      sortPairs.add(pair);
      tempMap.remove(c);
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < message.length(); i++) {
      builder.append(sortPairs.get(i % 3)[i / 3]);
    }

    String[] split = builder.toString().split("(?<=\\G[A-Z]{2}|[^A-Z])");
    return Arrays.stream(split)
        .map(this::decryptSymbol)
        .collect(Collectors.joining());
  }

  public static void main(String[] args) {
    String startMessage = "Hi! I am Alex:)";
    String key = "key";
    AdfgvxCipher adfgvxCipher = new AdfgvxCipher("D6EAM10IN3CBTYSWZ92LQOKVFG8HJPVX45R7", key);
    System.out.println("Encrypted message: " + adfgvxCipher.encrypt(startMessage));
    System.out.println(
        "Decrypted message: " + adfgvxCipher.decrypt(adfgvxCipher.encrypt(startMessage)));
  }
}
