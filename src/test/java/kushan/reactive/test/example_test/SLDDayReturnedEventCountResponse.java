package kushan.reactive.test.example_test;

import java.math.BigDecimal;

public class SLDDayReturnedEventCountResponse {
    long count1;
    long count2;
    long count3;

    public SLDDayReturnedEventCountResponse(long count1, long count2, long count3) {
        this.count1 = count1;
        this.count2 = count2;
        this.count3 = count3;
    }

    public long getCount1() {
        return count1;
    }

    public void setCount1(long count1) {
        this.count1 = count1;
    }

    public long getCount2() {
        return count2;
    }

    public void setCount2(long count2) {
        this.count2 = count2;
    }

    public long getCount3() {
        return count3;
    }

    public void setCount3(long count3) {
        this.count3 = count3;
    }

    @Override
    public String toString() {
        return "SLDDayReturnedEventCountResponse{" +
                "count1=" + count1 +
                ", count2=" + count2 +
                ", count3=" + count3 +
                '}';
    }
}
