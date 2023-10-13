package alex.serov.task2;

import alex.serov.Cipher;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Реализация шифра Виженера на основе интерфейса Cipher.
 * Шифр Виженера использует ключевое слово и процедуры шифрования и дешифрования для обработки сообщений.
 */
public class VigenereCipher implements Cipher {

  /**
   * Метод для шифрования или дешифрования сообщения с использованием ключа и заданного знака.
   *
   * @param message исходное сообщение для обработки.
   * @param key ключ для шифрования или дешифрования.
   * @param sign знак, определяющий направление обработки (шифрование или дешифрование).
   * @return зашифрованное или дешифрованное сообщение.
   */
  public static String crypt(String message, String key, int sign) {
    int keyLength = key.length();

    return IntStream.range(0, message.length())
        .mapToObj(i -> {
          char symbol = message.charAt(i);
          if (Character.isLetter(symbol)) {
            char base = Character.isLowerCase(symbol) ? 'а' : 'А';
            int messageChar = symbol - base;
            int keyChar = key.charAt(i % keyLength) - base;
            int cryptoChar = (messageChar + sign * keyChar + 33) % 33 + base;
            return (char) cryptoChar;
          } else {
            return symbol;
          }
        })
        .map(String::valueOf)
        .collect(Collectors.joining());
  }

  @Override
  public String encrypt(String message, String key) {
    return crypt(message, key, 1);
  }

  @Override
  public String decrypt(String message, String key) {
    return crypt(message, key, -1);
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.print("Message(rus): ");
    String startMessage = scanner.nextLine();

    System.out.print("Key(rus): ");
    String key = scanner.nextLine().replaceAll(" ", "");

    VigenereCipher vigenereCipher = new VigenereCipher();

    System.out.println("Encrypted message: " + vigenereCipher.encrypt(startMessage, key));
    System.out.println(
        "Decrypted message: " + vigenereCipher.decrypt(vigenereCipher.encrypt(startMessage, key),
            key));
  }
}
