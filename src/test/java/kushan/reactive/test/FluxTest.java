package kushan.reactive.test;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/*
  Reactive Streams
  1. Asynchronous
  2. Non-blocking
  3. Backpressure
  Publisher <- (subscribe) Subscriber
  Subscription is created
  Publisher (onSubscribe with the subscription) -> Subscriber
  Subscription <- (request N) Subscriber
  Publisher -> (onNext) Subscriber
  until:
  1. Publisher sends all the objects requested.
  2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be canceled
  3. There is an error. (onError) -> subscriber and subscription will be canceled
 */
public class FluxTest {
    //Creating the Logger object
    Logger log = LoggerFactory.getLogger(this.getClass());
    @Test
    public void fluxSubscriber(){
        Flux<String> flux = Flux.just("aa","bb","cc","dd")
                .log();

        flux.subscribe();

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectNext("aa","bb","cc","dd")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberConsumer(){
        Flux<Integer> flux = Flux.range(1,5)
                .log();

        flux.subscribe(integer -> log.info("Number is {}",integer));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeNumbersErrors(){
        Flux<Integer> flux = Flux.range(1,5)
                .log()
                .map(i -> {
                    if(i == 4){
                        throw new IndexOutOfBoundsException("error index");
                    }
                    return i;
                });

        flux.subscribe(i -> log.info("Number is {}",i),
                Throwable::printStackTrace,
                () -> log.info("COMPLETE!"),
                subscription -> subscription.request(3)); //when we use request count >= 4 we can get the

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscribeNumbersTestBackPressure(){
        Flux<Integer> flux = Flux.range(1,10)
                .log();

        flux.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(2);
            }

            @Override
            public void onNext(Integer integer) {
                log.info("Number is {}",integer);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                log.info("COMPLETE!");
            }
        });

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeNumbersUglyBackPressure(){
        Flux<Integer> flux = Flux.range(1,10)
                .log();

        flux.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            private int requestCount =2;
            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                log.info("Number is {}",integer);
                count++;
                if(count>= requestCount){
                    count = 0;
                    this.subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                log.info("COMPLETE!"); //this will print this time because publisher sends all the objects requested.
            }
        });

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeNumbersNotSoUglyBackPressure(){
        Flux<Integer> flux = Flux.range(1,10)
                .log();

        flux.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private int requestCount =2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                log.info("Number is {}",value);
                count++;
                if(count>= requestCount){
                    count = 0;
                    request(requestCount);
                }
            }

            @Override
            protected void hookOnComplete() {
                log.info("COMPLETE!"); //this will print this time because publisher sends all the objects requested.
            }
        });

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }



}
