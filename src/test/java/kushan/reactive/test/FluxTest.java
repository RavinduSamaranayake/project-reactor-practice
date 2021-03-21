package kushan.reactive.test;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
}
