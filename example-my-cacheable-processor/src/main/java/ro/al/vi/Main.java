package ro.al.vi;

import ro.al.vi.service.CalcService;
import ro.al.vi.service.KeyEnum;
import ro.al.vi.service.MyCacheableCalcService;

public class Main {

    private static final CalcService CALC_SERVICE = new MyCacheableCalcService();

    public static void main(String[] args) {
        KeyEnum key1 = KeyEnum.FIRST_KEY;
        Integer value1 = CALC_SERVICE.getIntByString(key1);
        System.out.println("key = " + key1 + ", value = " + value1);

        Long key2 = 16L;
        long start = System.nanoTime();
        Long value2 = CALC_SERVICE.getFactorial(key2);
        long end = System.nanoTime();
        System.out.println("EXAMPLE #1: " + (end - start) + " ns calc: key = " + key2 + ", value = " + value2);

        start = System.nanoTime();
        value2 = CALC_SERVICE.getFactorial(key2);
        end = System.nanoTime();
        System.out.println("EXAMPLE #2: " + (end - start) + " ns calc: key = " + key2 + ", value = " + value2);

        start = System.nanoTime();
        value2 = CALC_SERVICE.getFactorial(key2);
        end = System.nanoTime();
        System.out.println("EXAMPLE #3: " + (end - start) + " ns calc: key = " + key2 + ", value = " + value2);

        start = System.nanoTime();
        value2 = CALC_SERVICE.getFactorial(key2);
        end = System.nanoTime();
        System.out.println("EXAMPLE #4: " + (end - start) + " ns calc: key = " + key2 + ", value = " + value2);
    }
}