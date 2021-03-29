package kushan.reactive.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


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
public class OperatorsTest {
    Logger log = LoggerFactory.getLogger(this.getClass());
    @Test
    public void subscribeOnSimple(){
        //we are going to print 1-4 at 2 times.
        Flux<Integer> flux = Flux.range(1,4)
                .map(i -> {
                    log.info("Map1 Number : {} on Thread : {}",i,Thread.currentThread().getName());
                    return i;
                }).subscribeOn(Schedulers.elastic()) //when we do subscribeOn, we are not actually subscribing.
                .map(i -> {
                    log.info("Map2 Number : {} on Thread : {}", i, Thread.currentThread().getName());
                    return i;
                });

        //as you can see here when we apply subscribeOn here, it affect everything that publisher going to publish
        //even though I'm subscribeOn in the middle of these two maps, you can see that we are applying this for both of maps

        //flux.subscribe();

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void publishOnSimple(){
        //we are going to print 1-4 at 2 times.
        Flux<Integer> flux = Flux.range(1,4)
                //.publishOn(Schedulers.elastic())
                .map(i -> {
                    log.info("Map1 Number : {} on Thread : {}",i,Thread.currentThread().getName());
                    return i;
                }).publishOn(Schedulers.elastic()) //move this guy into the before 1st map and test it again
                .map(i -> {
                    log.info("Map2 Number : {} on Thread : {}", i, Thread.currentThread().getName());
                    return i;
                });

        //as you can see here the first map executing on main thread and 2nd map executing given scheduler thread
        //even though I'm subscribeOn in the middle of these two maps, you can see that we are applying these for both of maps

        //flux.subscribe();
        //flux.subscribe(); //many times we are subscribing, threads created according to scheduling type
        //flux.subscribe();


        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }
    //what it's important to understand here first the subscribeOn it is applied to the subscription process if you places subscribeOn in a change -
    //- it will affect to the source emission in the entire change
    //However the publisher not affect to the entire change as you can see, publishOn will only affect what we have after .publishOn(Schedulers.elastic()) part in the change
    //so it will affect only the subsequent operators that we have after on publishOn

    @Test
    public void multipleSubscribeOnSimple(){
        Flux<Integer> flux = Flux.range(1,4)
                .subscribeOn(Schedulers.single()) //this scheduler is effect to the entire publisher
                .map(i -> {
                    log.info("Map1 Number : {} on Thread : {}",i,Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.elastic())
                .map(i -> {
                    log.info("Map2 Number : {} on Thread : {}", i, Thread.currentThread().getName());
                    return i;
                });

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void multiplePublishOnSimple(){
        Flux<Integer> flux = Flux.range(1,4)
                .publishOn(Schedulers.single()) //this scheduler is effect to the map1
                .map(i -> {
                    log.info("Map1 Number : {} on Thread : {}",i,Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.elastic()) //this scheduler is effect to the map2
                .map(i -> {
                    log.info("Map2 Number : {} on Thread : {}", i, Thread.currentThread().getName());
                    return i;
                });

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    //when we use subscriberOn it affects every thing that we emitted and publisherOn its going to affects the operators that are subsequence to it.


    @Test
    public void publishAndSubscribeOnSimple(){
        Flux<Integer> flux = Flux.range(1,4)
                .publishOn(Schedulers.single()) //this scheduler is affecting every thing
                .map(i -> {
                    log.info("Map1 Number : {} on Thread : {}",i,Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.elastic()) // this is not been affected because this scheduler publishing this on single thread
                .map(i -> {
                    log.info("Map2 Number : {} on Thread : {}", i, Thread.currentThread().getName());
                    return i;
                });

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void subscribeAndPublishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .subscribeOn(Schedulers.single()) //this scheduler is affecting to map1
                .map(i -> {
                    log.info("Map1 Number : {} on Thread : {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.elastic()) //this scheduler is affecting to map2
                .map(i -> {
                    log.info("Map2 Number : {} on Thread : {}", i, Thread.currentThread().getName());
                    return i;
                });

        log.info("-------------------test the code using reactor-test step verifier---------------------------");

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void subscribeOnIO() throws InterruptedException {
        Mono<List<String>> listMono = Mono.fromCallable(() -> Files.readAllLines(Paths.get("text-file")))
                .log()
                .subscribeOn(Schedulers.single());

     //   listMono.subscribe(s -> log.info("File values : {}",s)); // this is happening on background thread -
       // Thread.sleep(2000); //-so you can see the file values when only sleep the main thread like this

        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(listMono)
                .expectSubscription()
                .thenConsumeWhile(l -> {
                    Assertions.assertFalse(l.isEmpty()); //Let's assert listMono is not empty
                    log.info("List Size {} :",l.size());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void switchIfEmptyOperator(){
        Flux flux = emptyFlux()
                .switchIfEmpty(Flux.just("not empty any more"))
                .log();

        //Use case Ex: You are executing some thing to save in the DB if the search return empty flux, use switchIfEmpty and save this object
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("not empty any more")
                .verifyComplete();

    }

    @Test
    public void deferOperator() throws InterruptedException {
        Mono<Long> just = Mono.just(System.currentTimeMillis());
        log.info("....test with mono just....");
        just.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        just.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        just.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        just.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);

        Mono<Long> deffer = Mono.defer(() -> Mono.just(System.currentTimeMillis())); // this lambda will be executed each time subscriber subscribes to this deffer
        log.info("....test with defer operator....");
        deffer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        deffer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        deffer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        deffer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);

        AtomicLong atomicLong = new AtomicLong();
        deffer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get()>0);

    }

    private Flux<Object> emptyFlux(){
        return Flux.empty();
    }
}
