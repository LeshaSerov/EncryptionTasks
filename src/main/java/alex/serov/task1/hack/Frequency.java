package alex.serov.task1.hack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Класс, представляющий собой набор методов для работы с частотами монограмм, биграмм и триграмм.
 */
public class Frequency {

  // Статические поля, представляющие собой частоты монограмм, биграмм и триграмм.
  public static Map<String, Double> monogramFrequency = put("src/main/resources/monoFrequency.txt");
  public static Map<String, Double> bigramFrequency = put("src/main/resources/biFrequency.txt");
  public static Map<String, Double> trigramFrequency = put("src/main/resources/triFrequency.txt");

  /**
   * Метод для чтения частот из файла и формирования отображения.
   *
   * @param filePath путь к файлу с частотами.
   * @return отображение с частотами.
   */
  private static Map<String, Double> put(String filePath) {
    Map<String, Double> map = new LinkedHashMap<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\t"); // Разделяем на ключ и значение
        map.put(parts[0].toLowerCase(), Double.parseDouble(parts[1].replace(",", ".")) / 100);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return map;
  }

  /**
   * Метод для получения подмножества n-грамм, содержащих указанный символ.
   *
   * @param ngramFrequencies отображение с частотами n-грамм.
   * @param findString символ, который должен содержаться в n-грамме.
   * @return отображение с подмножеством n-грамм.
   */
  public static Map<String, Double> getNgramsWithChar(Map<String, Double> ngramFrequencies, String findString) {
    Map<String, Double> result = new HashMap<>();
    for (String ngram : ngramFrequencies.keySet()) {
      if (ngram.contains(findString)) {
        result.put(ngram, ngramFrequencies.get(ngram));
      }
    }
    return result;
  }
}
