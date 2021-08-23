package kushan.reactive.test.example_test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class TestExOne {

    public static void main(String[] args) {
        AtomicLong dayReturnedCount = new AtomicLong(0L);
        AtomicLong lastTxReturnedCount = new AtomicLong(0L);
        AtomicLong updatedEODSeq = new AtomicLong(0L);
        AtomicLong lastUpdatedTime = new AtomicLong(0L);
        Mono<Long> valMn = Mono.just(0L);



        Flux<SLDDayReturnedEventCountResponse> responseFlux = valMn.flatMapMany(initSeq -> Flux.just(1,2,3))
                .doOnNext(e -> {
                    dayReturnedCount.set(0L);
                    lastTxReturnedCount.set(0L);
                    lastUpdatedTime.set(0L);
                    System.out.println("____________________________________________________");
                })
                .flatMap(eodEvent -> valMn)
                .flatMap(latestPrevEodSeq -> {
                    updatedEODSeq.set(latestPrevEodSeq);
                    System.out.println("Latest eodSeq : " + latestPrevEodSeq);
                    return Flux.just("Hi", "Hey", "Hello", "Lol", "Bye")
                            .doOnNext(loanReturned -> {
                                dayReturnedCount.set(Long.sum(dayReturnedCount.get(), 1));
                                System.out.println("Loan Returned val : " + loanReturned);
                            })
                            .flatMap(loanReturned -> Mono.just("Hello I'm Mono.."))
                            .doOnNext(str -> System.out.println("Str is : " + str))
                            .map(val -> new SLDDayReturnedEventCountResponse(dayReturnedCount.get(),
                                    lastUpdatedTime.get(), lastTxReturnedCount.get()));
                });


        responseFlux.subscribe(val -> System.out.println("Final Obj Value is : "+val.toString()));


    }
}
