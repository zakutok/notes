package strings.lsd;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class LeastSignificantDigit {

    public static void main(String[] args) {
        var input = ImmutableList.of(
                "possible",
                "continue",
                "electric",
                "politics",
                "occasion",
                "medicine",
                "industry",
                "electric",
                "ceremony",
                "occasion",
                "heavenly"
        );

        var length = 8;

        sort(input, length);
    }

    private static void sort(ImmutableList<String> input, int length) {

        var data = new ArrayList<>(input);

        for (var i = length - 1; i >= 0; i--) {
            kic(data, i);
        }
    }

    private static void kic(List<String> data, int i) {
        var counts = new int[257];

        for (var s : data) {
            counts[s.charAt(i)]++;
        }

        for (var j = 1; j < counts.length; j++) {
            counts[j] += counts[j - 1];
        }

        var aux = new String[data.size()];

        for (var s : data) {
            var index = counts[s.charAt(i) - 1]++;
            aux[index] =  s;
        }

        for (int k = 0; k < aux.length; k++) {
            data.set(k, aux[k]);
        }

        System.out.println(data);
    }
}
