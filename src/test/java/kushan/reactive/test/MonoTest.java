package kushan.reactive.test;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MonoTest {
    //Creating the Logger object
    Logger log = LoggerFactory.getLogger(this.getClass());
    @Test
    public void monoSubscriber(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name);
     //           .log();
        mono.subscribe();

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name)
                .log();
        mono.subscribe(s -> log.info("Value is : {}",s));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException();
                });

        mono.subscribe(s -> log.info("Value is : {}",s),s -> log.error("some thing went wrong"));
        mono.subscribe(s -> log.info("Value is : {}",s),Throwable::printStackTrace);

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }
}
