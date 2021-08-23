package kushan.reactive.test.example_test;

import kushan.reactive.test.example_test.SLDDayReturnedEventCountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class TestExThree {

    public static void main(String[] args) {
        AtomicLong dayReturnedCount = new AtomicLong(0L);
        AtomicLong lastTxReturnedCount = new AtomicLong(0L);
        AtomicLong updatedEODSeq = new AtomicLong(0L);
        AtomicLong lastUpdatedTime = new AtomicLong(0L);
        Mono<Long> valMn = Mono.just(0L);

        Mono<SLDDayReturnedEventCountResponse> initResponse = Mono.just(34).flatMapMany(lastEodSeq -> Flux.just(12L,15L,17L,20L,23L,45L))
                .doOnNext(resultMap -> {
                    dayReturnedCount.set(Long.sum(dayReturnedCount.get(), 1));
                    System.out.println("Value is : "+resultMap);
                })
                .last().flatMap(resultMap -> Mono.just("H23")
                                .doOnNext(count -> {
                                    System.out.println("Last Value : "+resultMap);
                                    lastUpdatedTime.set(resultMap);
                                }).map(val -> new SLDDayReturnedEventCountResponse(90L,100L,200L)));



        Flux<SLDDayReturnedEventCountResponse> responseFlux = valMn.flatMapMany(initSeq -> Flux.just(1))
                .doOnNext(e -> {
                    //dayReturnedCount.set(0L);
                    lastTxReturnedCount.set(0L);
                    System.out.println("Last Tx time : " + lastUpdatedTime.get());
                    System.out.println("____________________________________________________");
                })
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
        Flux.concat(initResponse,responseFlux).subscribe(val -> System.out.println("Final Obj Value is : "+val.toString()));

    }
}
