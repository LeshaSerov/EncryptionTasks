package alex.serov.task1.hack;

import alex.serov.task1.SingleAlphabetReplacementCipherImpl;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Класс для взлома шифра однозначной алфавитной замены.
 */
public class HackCipherOneAlphaSubstitution {

  /**
   * Словарь, используемый для хранения замененных символов в процессе взлома.
   */
  static Map<Character, Character> cipher = new HashMap<>();

  static {
    cipher.put('a', 'x');
    cipher.put('b', 'y');
    cipher.put('c', 'z');
    cipher.put('d', 'a');
    cipher.put('e', 'b');
    cipher.put('f', 'c');
    cipher.put('g', 'd');
    cipher.put('h', 'e');
    cipher.put('i', 'f');
    cipher.put('j', 'g');
    cipher.put('k', 'h');
    cipher.put('l', 'i');
    cipher.put('m', 'j');
    cipher.put('n', 'k');
    cipher.put('o', 'l');
    cipher.put('p', 'm');
    cipher.put('q', 'n');
    cipher.put('r', 'o');
    cipher.put('s', 'p');
    cipher.put('t', 'q');
    cipher.put('u', 'r');
    cipher.put('v', 's');
    cipher.put('w', 't');
    cipher.put('x', 'u');
    cipher.put('y', 'v');
    cipher.put('z', 'w');
  }

  /**
   * Метод для взлома шифра однозначной алфавитной замены.
   *
   * @param encodedString Закодированная строка.
   * @return Словарь, представляющий соответствие между оригинальными и замененными символами.
   */
  static Map<Character, Character> hack(String encodedString) {
    Map<String, Double> monogram = generateNgramFrequencyKeys(encodedString, 1);
    Map<String, Double> bigram = generateNgramFrequencyKeys(encodedString, 2);
    Map<String, Double> trigram = generateNgramFrequencyKeys(encodedString, 3);
    Map<Character, Character> cipher = new HashMap<>();
    double errorThreshold = 0.08;
    Set<String> set = new HashSet<>();
    for (String c: Frequency.monogramFrequency.keySet()) {
      List<String> options = FrequencyMatcher.findClosestMatches(
          Frequency.monogramFrequency.get(c), monogram, set, 10);
      System.out.print("Options for " + c + ": " + options + " -> ");

      Map<String, Integer> resultMap = new HashMap<>();
      for (String option : options) {
        int matchCount = 0;

        Map<String, Double> correspondingEncodeBigram = Frequency.getNgramsWithChar(bigram, option);
        Map<String, Double> correspondingDecodeBigram = Frequency.getNgramsWithChar(
            Frequency.bigramFrequency, option);
        for (String sBigram : correspondingEncodeBigram.keySet()) {
          double encodeFrequency = correspondingEncodeBigram.get(sBigram);
          double decodeFrequency = correspondingDecodeBigram.getOrDefault(sBigram, 0.0);
          if (Math.abs(encodeFrequency - decodeFrequency) < errorThreshold) {
            matchCount++;
          }
        }
        Map<String, Double> correspondingEncodeTrigram = Frequency.getNgramsWithChar(trigram,
            option);
        Map<String, Double> correspondingDecodeTrigram = Frequency.getNgramsWithChar(
            Frequency.trigramFrequency, option);
        for (String sTrigram : correspondingEncodeTrigram.keySet()) {
          double encodeFrequency = correspondingEncodeTrigram.get(sTrigram);
          double decodeFrequency = correspondingDecodeTrigram.getOrDefault(sTrigram, 0.0);
          if (Math.abs(encodeFrequency - decodeFrequency) < errorThreshold) {
            matchCount++;
          }
        }

        if (matchCount
            >= (correspondingDecodeBigram.size() + correspondingDecodeTrigram.size()) / 50) {
          if (!set.contains(c)) {

            resultMap.put(option, matchCount);
          }
        }
      }

      if (!resultMap.isEmpty()) {
        List<Entry<String,Integer>> matches = resultMap.entrySet()
            .stream()
            .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(3).toList();
        for (var e : matches)
        {
          System.out.print(e.getKey() + " ");
        }

        String maxKey = resultMap.entrySet()
            .stream()
            .max(Entry.comparingByValue())
            .map(Entry::getKey)
            .orElse(null);

        cipher.computeIfAbsent(c.charAt(0), k -> maxKey.charAt(0));
        set.add(maxKey);
        System.out.println(": " + cipher.get(c.charAt(0)));
      } else {
        System.out.println("-");
      }
    }

    return cipher;
  }

  /**
   * Генерирует частотные ключи для n-грамм (последовательности из n символов) в тексте.
   *
   * @param text Текст, для которого генерируются ключи.
   * @param n    Число, представляющее длину n-грамм.
   * @return Map, содержащая частоты для каждой уникальной n-граммы, отсортированную по убыванию
   * частот.
   */
  public static Map<String, Double> generateNgramFrequencyKeys(String text, int n) {
    Map<String, Double> ngramCountMap = new HashMap<>();
    for (int i = 0; i < text.length() - n + 1; i++) {
      String ngram = text.substring(i, i + n);
      if (ngram.matches("[a-zA-Z]{" + n + "}")) {
        ngramCountMap.merge(ngram, 1.0, Double::sum);
      }
    }

    return sortAndNormalizeMap(ngramCountMap);
  }

  /**
   * Сортирует и нормализует заданную Map по значениям в порядке убывания.
   *
   * @param map Map, которую необходимо отсортировать и нормализовать.
   * @return Новая Map, содержащая отсортированные и нормализованные значения из входной Map.
   */
  private static Map<String, Double> sortAndNormalizeMap(Map<String, Double> map) {
    map = map.entrySet()
        .stream()
        .sorted(Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
        .collect(Collectors.toMap(
            Entry::getKey,
            Entry::getValue,
            (e1, e2) -> e2,
            LinkedHashMap::new
        ));
    double totalOccurrences = map.values().stream().mapToDouble(Double::doubleValue).sum();
    map.replaceAll((k, v) -> v / totalOccurrences);
    return map;
  }

  public static void main(String[] args) {
    //В случае больших текстов получается только около 10-15 символов.
    String message = """
                One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin. He lay on his armour-like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections. The bedding was hardly able to cover it and seemed ready to slide off any moment. His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked. "What's happened to me?" he thought. It wasn't a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls. A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame. It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer. Gregor then turned to look out the window at the dull weather. Drops of rain could be heard hitting the pane, which made him feel quite sad. "How about if I sleep a little bit longer and forget all this nonsense", he thought, but that was something he was unable to do because he was used to sleeping on his right, and in his present state couldn't get into that position. However hard he threw himself onto his right, he always rolled back to where he was. He must have tried it a hundred times, shut his eyes so that he wouldn't have to look at the floundering legs, and only stopped when he began to feel a mild, dull pain there that he had never felt before. "Oh, God", he thought, "what a strenuous career it is that I've chosen! Travelling day in and day out. Doing business like this takes much more effort than doing your own business at home, and on top of that there's the curse of travelling, worries about making train connections, bad and irregular food, contact with different people all the time so that you can never get to know anyone or become friendly with them. It can all go to Hell!" He felt a slight itch up on his belly; pushed himself slowly up on his back towards the headboard so that he could lift his head better; found where the itch was, and saw that it was covered with lots of little white spots which he didn't know what to make of; and when he tried to feel the place with one of his legs he drew it quickly back because as soon as he touched it he was overcome by a cold shudder. He slid back into his former position. "Getting up early all the time", he thought, "it makes you stupid. You've got to get enough sleep. Other travelling salesmen live a life of luxury. For instance, whenever I go back to the guest house during the morning to copy out the contract, these gentlemen are always still sitting there eating their breakfasts. I ought to just try that with my boss; I'd get kicked out on the spot. But who knows, maybe that would be the best thing for me. If I didn't have my parents to think about I'd have given in my notice a long time ago, I'd have gone up to the boss and told him just what I think, tell him everything I would, let him know just what I feel. He'd fall right off his desk! And it's a funny sort of business to be sitting up there at your desk, talking down at your subordinates from up there, especially when you have to go right up close because the boss is hard of hearing. Well, there's still some hope; once I've got the money together to pay off my parents' debt to him - another five or six years I suppose - that's definitely what I'll do. That's when I'll make the big change. First of all though, I've got to get up, my train leaves at five." And he looked over at the alarm clock, ticking on the chest of drawers. "God in Heaven!" he thought. It was half past six and the hands were quietly moving forwards, it was even later than half past, more like quarter to seven. Had the alarm clock not rung? He could see from the bed that it had been set for four o'clock as it should have been; it certainly must have rung. Yes, but was it possible to quietly sleep through that furniture-rattling noise? True, he had not slept peacefully, but probably all the more deeply because of that. What should he do now? The next train went at seven; if he were to catch that he would have to rush like mad and the collection of samples was still not packed, and he did not at all feel particularly fresh and lively. And even if he did catch the train he would not avoid his boss's anger as the office assistant would have been there to see the five o'clock train go, he would have put in his report about Gregor's not being there a long time ago. The office assistant was the boss's man, spineless, and with no understanding. What about if he reported sick? But that would be extremely strained and suspicious as in fifteen years of service Gregor had never once yet been ill. His boss would certainly come round with the doctor from the medical insurance company, accuse his parents of having a lazy son, and accept the doctor's recommendation not to make any claim as the doctor believed that no-one was ever ill but that many were workshy. And what's more, would he have been entirely wrong in this case? Gregor did in fact, apart from excessive sleepiness after sleeping for so long, feel completely well and even felt much hungrier than usual. One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin. He lay on his armour-like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections. The bedding was hardly able to cover it and seemed ready to slide off any moment. His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked. "What's happened to me?" he thought. It wasn't a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls. A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame. It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer. Gregor then turned to look out the window at the dull weather. Drops of rain could be heard hitting the pane, which made him feel quite sad. "How about if I sleep a little bit longer and forget all this nonsense", he thought, but that was something he was unable to do because he was used to sleeping on his right, and in his present state couldn't get into that position. However hard he threw himself onto his right, he always rolled back to where he was. He must have tried it a hundred times, shut his eyes so that he wouldn't have to look at the floundering legs, and only stopped when he began to feel a mild, dull pain there that he had never felt before. "Oh, God", he thought, "what a strenuous career it is that I've chosen! Travelling day in and day out. Doing business like this takes much more effort than doing your own business at home, and on top of that there's the curse of travelling, worries about making train connections, bad and irregular food, contact with different people all the time so that you can never get to know anyone or become friendly with them. It can all go to Hell!" He felt a slight itch up on his belly; pushed himself slowly up on his back towards the headboard so that he could lift his head better; found where the itch was, and saw that it was covered with lots of little white spots which he didn't know what to make of; and when he tried to feel the place with one of his legs he drew it quickly back because as soon as he touched it he was overcome by a cold shudder. He slid back into his former position. "Getting up early all the time", he thought, "it makes you stupid. You've got to get enough sleep. Other travelling salesmen live a life of luxury. For instance, whenever I go back to the guest house during the morning to copy out the contract, these gentlemen are always still sitting there eating their breakfasts. I ought to just try that with my boss; I'd get kicked out on the spot. But who knows, maybe that would be the best thing for me. If I didn't have my parents to think about I'd have given in my notice a long time ago, I'd have gone up to the boss and told him just what I think, tell him everything I would, let him know just what I feel. He'd fall right off his desk! And it's a funny sort of business to be sitting up there at your desk, talking down at your subordinates from up there, especially when you have to go right up close because the boss is hard of hearing. Well, there's still some hope; once I've got the money together to pay off my parents' debt to him - another five or six years I suppose - that's definitely what I'll do. That's when I'll make the big change. First of all though, I've got to get up, my train leaves at five." And he looked over at the alarm clock, ticking on the chest of drawers. "God in Heaven!" he thought. It was half past six and the hands were quietly moving forwards, it was even later than half past, more like quarter to seven. Had the alarm clock not rung? He could see from the bed that it had been set for four o'clock as it should have been; it certainly must have rung. Yes, but was it possible to quietly sleep through that furniture-rattling noise? True, he had not slept peacefully, but probably all the more deeply because of that. What should he do now? The next train went at seven; if he were to catch that he would have to rush like mad and the collection of samples was still not packed, and he did not at all feel particularly fresh and lively. And even if he did catch the train he would not avoid his boss's anger as the office a
                """;
    String key = "xyzabcdefghijklmnopqrstuvw";
    SingleAlphabetReplacementCipherImpl cipher = new SingleAlphabetReplacementCipherImpl(key);
    System.out.println("Fey: " + hack(cipher.encrypt(message)));
    System.out.println("Key: " + HackCipherOneAlphaSubstitution.cipher);
  }
}
