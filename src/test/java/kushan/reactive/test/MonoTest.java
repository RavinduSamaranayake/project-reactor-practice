package kushan.reactive.test;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

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
    public void monoBlockTest(){
        String name = "123";
        long val = Mono.just(name)
                .map(e -> getQval(e) == null ? 0L : Long.parseLong(e))
                .blockOptional().orElse(50L);

        System.out.println("val is : " + val);

    }

    public String getQval(String val){
        return null;
    }

    @Test
    public void monoSubscriberConsumerError(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> {
                    throw new RuntimeException();
                });

        mono.subscribe(s -> log.info("Value is : {}",s)
                ,s -> log.error("some thing went wrong"),
                () -> log.info("FINISHED"));//do not print this due to error

        log.info("---------With Print StackTrace--------");
        mono.subscribe(s -> log.info("Value is : {}",s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value is : {}",s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));//on complete

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value is : {}",s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                Subscription::cancel);

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscriptionRequest(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value is : {}",s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods(){
        String name = "Kushan Ravindu";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed...!"))
                .doOnRequest(longNumber -> log.info("Request Received with count : {}, starting doing some thing...!",longNumber))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext : {}",s))
                .doOnSuccess(s -> log.info("doOnSuccess Executed {}",s));

        mono.subscribe(s -> log.info("Value is : {}",s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5));

//        log.info("-------------------test the code using reactor-test step verifier---------------------------");
//
//        StepVerifier.create(mono)
//                .expectNext(name.toUpperCase())
//                .verifyComplete();
    }

    @Test
    public void monoDoOnMethodsWithFlatMap(){
        String name = "Kushan Ravindu";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed...!"))
                .doOnRequest(longNumber -> log.info("Request Received with count : {}, starting doing some thing...!",longNumber))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext : {}",s))
                .flatMap(s -> Mono.empty())//if use map for this time, it returns Mono<Mono<Object>> that's why we use flat map for this case
                .doOnNext(s -> log.info("Value is here. Executing doOnNext : {}",s))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext : {}",s))
                .doOnSuccess(s -> log.info("doOnSuccess Executed {}",s));

        mono.subscribe(s -> log.info("Value is : {}",s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5));

//        log.info("-------------------test the code using reactor-test step verifier---------------------------");
//
//        StepVerifier.create(mono)
//                .expectNext(name.toUpperCase())
//                .verifyComplete();
    }

    @Test
    public void monoDoOnError(){
        Mono<Object> monoErr = Mono.error(new Exception("My Custom Exception......"))
                .doOnError(e -> log.error("Error Msg is : {}",e.getMessage()))
                .doOnNext(s -> log.info("Executed doOnNext....."))// this is not Executed due to termination on doOnError
                .log();


        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(monoErr)
                .expectError(Exception.class)
                .verify();
    }

    @Test
    public void monoOnErrorResume(){ // this is same as java handling exceptions
        String name = "Kushan Ravindu";
        Mono<Object> monoErr = Mono.error(new Exception("My Custom Exception......"))
                .doOnError(e -> log.error("Error Msg is : {}",e.getMessage()))
                .onErrorResume(s -> { //like java catch clause
                    log.info("Executed onErrorResume....."); //execute this as well as after handling exceptions
                    return Mono.just(name);
                })
                .log();


        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(monoErr)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoOnErrorReturn(){ // this is same as monoOnErrorResume() and use onErrorReturn() for instead of onErrorResume
        String name = "Kushan Ravindu";
        Mono<Object> monoErr = Mono.error(new Exception("My Custom Exception......"))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Executed onErrorResume.....");
                    return Mono.just(name);
                })
                .doOnError(e -> log.error("Error Msg is : {}",e.getMessage()))
                .log();


        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(monoErr)
                .expectNext("EMPTY")
                .verifyComplete();
    }

}
