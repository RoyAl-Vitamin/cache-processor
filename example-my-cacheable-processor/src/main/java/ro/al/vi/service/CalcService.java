package ro.al.vi.service;

import ro.al.vi.cache.annotation.MyCacheable;

public class CalcService {

    @MyCacheable
    public Integer getIntByString(KeyEnum key) {
        return switch (key) {
            case FIRST_KEY -> 1;
            case SECOND_KEY -> 2;
            case THIRD_KEY -> 3;
            case null, default -> 0;
        };
    }

    @MyCacheable(key = Long.class, value = Long.class, isThreadSafe = false)
    public Long getFactorial(Long i) {
        if (i <= 1) {
            return 1L;
        }
        return i * getFactorial(i - 1);
    }

    @MyCacheable
    public void getVoidByString(KeyEnum key) {
    }
}
