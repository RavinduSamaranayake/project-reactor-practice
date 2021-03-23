package kushan.reactive.test;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

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

    @Test
    public void fluxSubscriberPrettyBackPressure(){
        Flux<Integer> flux = Flux.range(1,10)
                .log();

        flux.limitRate(3)
                .subscribe(integer -> log.info("Number is {}",integer));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberInterval() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .log(); //*as you can see we don't have any thing because this is some thing that going to block your thread

        //so the guys from reactor they are smart and they decided to have this interval running on a background thread
        interval.subscribe(i -> log.info("Number is {} : ",i));//stating from 0 and print while thread is alive

        //to fix this *, we sleep the main thread for some time
        //after doing this we can see every 100 milliseconds you get things published until the main thread dies
        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalWithTake() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10) // Now we can see the onComplete() triggered after printing 10 elements
                .log();

        interval.subscribe(i -> log.info("Number is {} : ",i));

        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTest() {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofDays(1))
                .thenAwait(Duration.ofDays(1))//wait for a day
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1))
                .log();
    }



    @Test //HOT flux
    public void connectableFlux() throws InterruptedException {
        //This is how we can have hot observables so it will execute at the moment you are connecting even if you not have any subscribers.

        ConnectableFlux<Integer> connectableFlux = Flux.range(1,10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish();

        connectableFlux.connect(); //Now subscriber doesn't need to exists for this to get events immediately

        //see how its works by main thread sleeping

//        log.info("Thread Sleeping for 100ms");
//
//        Thread.sleep(100);
//
//        connectableFlux.subscribe(i -> log.info("Sub 1 number : {}",i));
//
//        log.info("Thread Sleeping for 200ms");
//
//        Thread.sleep(200);
//
//        connectableFlux.subscribe(i -> log.info("Sub 2 number : {}",i));
//
//        log.info("Thread Sleeping for 300ms");
//
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i -> log.info("Sub 3 number : {}",i));
//
//        log.info("Thread Sleeping for 300ms");
//
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i -> log.info("Sub 4 number : {}",i));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

//        StepVerifier.create(connectableFlux)
//                .then(connectableFlux::connect)
//                .expectNext(1,2,3,4,5,6,7,8,9,10)
//                .verifyComplete();

        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(i -> i <= 5) //this means we are going to lose 1,2,3,4,5 values
                .expectNext(6,7,8,9,10)
                .verifyComplete();
    }

    @Test //HOT flux
    public void connectableFluxAutoConnect() {
        Flux<Integer> connectableFlux = Flux.range(1,10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(2);

    //    connectableFlux.subscribe(integer -> log.info("Number is {}",integer));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

//        StepVerifier.create(connectableFlux)
//                .then(connectableFlux::connect)
//                .expectNext(1,2,3,4,5,6,7,8,9,10)
//                .verifyComplete();

        StepVerifier.create(connectableFlux)
                .then(connectableFlux::subscribe) //when we execute without (connectableFlux::subscribe), this will hang on forever because this is expecting the 2nd subscriber
                .thenConsumeWhile(i -> i <= 5) //this means we are going to lose 1,2,3,4,5 values
                .expectNext(6,7,8,9,10)
                .verifyComplete();
    }


}
