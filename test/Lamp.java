public class Lamp {
    public static void main(String[] args) {
        java.util.Arrays
                .asList(
                        new Boolean[] {Boolean.TRUE, Boolean.FALSE})
                .forEach((x) -> System.out.println(
                        ((java.util.function.Function<Boolean,String>)(y) -> y ? "Y" : "N")
                                .apply(x)));
    }
}
