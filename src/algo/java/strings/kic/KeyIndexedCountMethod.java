package strings.kic;

import java.util.Arrays;

public class KeyIndexedCountMethod {

    public static void main(String[] args) {

        Object[][] input = {
                {"a", 4},
                {"b", 2},
                {"c", 4},
                {"d", 1},
                {"e", 2},
                {"f", 3},
                {"g", 1},
        };

        var maxIndex = Arrays.stream(input).map(o -> (int) o[1]).max(Integer::compareTo).get();

        sort(input, maxIndex);

        System.out.println(Arrays.deepToString(input));
    }

    private static void sort(Object[][] input, int maxIndex) {
        var counts = new int[maxIndex + 1];

        for (var e : input) {
            var index = (int) e[1];
            counts[index]++;
        }
        System.out.println(Arrays.toString(counts));

        for (var i = 1; i < counts.length; i++) {
            counts[i] += counts[i - 1];
        }
        System.out.println(Arrays.toString(counts));

        var aux = new Object[input.length][input[1].length];

        for (var e : input) {
            var index = counts[(int) e[1] - 1]++;
            aux[index][0] = e[0];
            aux[index][1] = e[1];
        }
        System.out.println(Arrays.deepToString(aux));

        for (var i = 0; i < aux.length; i++) {
            input[i][0] = aux[i][0];
            input[i][1] = aux[i][1];
        }
    }
}
