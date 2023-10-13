package alex.serov.task1.hack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Класс для нахождения трех наилучших соответствий для заданной частоты.
 */
public class FrequencyMatcher {

  /**
   * Метод для нахождения наилучших соответствий для заданной частоты.
   *
   * @param input              входная частота.
   * @param decryptedFrequencies отображение с расшифрованными частотами символов.
   * @param set                множество символов, которые нужно исключить из рассмотрения.
   * @param count              количество выводимых вариантов
   * @return список с тремя наилучшими соответствиями.
   */
  public static List<String> findClosestMatches(double input,
      Map<String, Double> decryptedFrequencies,
      Set<String> set,
      Integer count) {
    // Сортируем символы по близости к входному значению
    List<Entry<String, Double>> closestMatches = decryptedFrequencies.entrySet()
        .stream()
        .filter(x -> set.stream().noneMatch(y -> Objects.equals(x.getKey(), y)))
        .sorted(Comparator.comparingDouble(x -> Math.abs(x.getValue() - input)))
        .limit(count)
        .toList();

    List<String> list = new ArrayList<>();
    for (var e : closestMatches) {
      list.add(e.getKey());
    }
    return list;
  }
}
