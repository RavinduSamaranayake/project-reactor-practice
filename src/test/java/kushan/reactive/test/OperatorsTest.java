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
import java.nio.file.Paths;
import java.time.Duration;
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
        log.info("-------------------test the code using reactor-test step verifier---------------------------");
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

        log.info("-------------------test the code---------------------------");

        AtomicLong atomicLong = new AtomicLong();
        deffer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get()>0);

    }

    @Test
    public void concatOperator(){
        Flux<String> flux1 = Flux.just("a","b");
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> concatFlux = Flux.concat(flux1,flux2).log();

        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","b","c","d")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatWithOperator(){
        Flux<String> flux1 = Flux.just("a","b");
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> concatFlux = flux1.concatWith(flux2).log();
        concatFlux.subscribe(s -> System.out.println("-------"+s));

        log.info("-------------------test the code using reactor-test step verifier---------------------------");
//        StepVerifier.create(concatFlux)
//                .expectSubscription()
//                .expectNext("a","b","c","d")
//                .expectComplete()
//                .verify();
    }

    @Test
    public void concatOperatorWithError(){
        Flux<String> flux1 = Flux.just("a","b")
                .map(s -> {
                    if(s.equals("b")){
                        throw new IllegalArgumentException();
                    }
                    return s;
                });

        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> concatFlux = Flux.concatDelayError(flux1,flux2).log();

        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","c","d")
                .expectError()
                .verify();
    }

    @Test
    public void combineLatestOperator(){
        Flux<String> flux1 = Flux.just("a","b");
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> combineLatest = Flux.combineLatest(flux1,flux2,
                (s1,s2) -> s1.toUpperCase()+"-"+s2.toUpperCase())
                .log();

        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(combineLatest)
                .expectSubscription()
                .expectNext("B-C","B-D")
                .expectComplete()
                .verify();

    }

    @Test
    public void mergeOperator() throws InterruptedException {
        Flux<String> flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c","d");
        //The Flux is going to subscribe eagerly. it means that not wait until first one finishes
        // And second that takes run in different threads

        Flux<String> mergeFlux = Flux.merge(flux1,flux2)
                .delayElements(Duration.ofMillis(200))
                .log();

     //   mergeFlux.subscribe(log::info);

   //     Thread.sleep(1000);

        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();

    }

    //The merge will work in a way to similar to the concat
    //difference of merge and concat :
    //concat is the lazy provider. it means it will wait until the first publisher finishes and then it will start to the second one

    @Test
    public void mergeWithOperator() {
        Flux<String> flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> mergeFlux = flux1.mergeWith(flux2)
                .delayElements(Duration.ofMillis(200))
                .log();

        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();

    }

    @Test
    public void mergeSequentialOperator() {
        Flux<String> flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> mergeFlux = Flux.mergeSequential(flux1,flux2,flux1).log();
        //Flux<String> mergeFlux = Flux.merge(flux1,flux2,flux1).log();


        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("a","b","c","d","a","b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWithErrorOperator() {
        Flux<String> flux1 = Flux.just("a","b").map(s -> {
            if(s.equals("b")){
                throw new IllegalArgumentException();
            }
            return s;
        }).doOnError(t -> log.info("We Could do some thing with this..."));

        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> mergeFlux = Flux.mergeDelayError(1,flux1,flux2,flux1).log();


        log.info("-------------------test the code using reactor-test step verifier---------------------------");
        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("a","c","d","a")
                .expectError()
                .verify();
    }

    private Flux<Object> emptyFlux(){
        return Flux.empty();
    }
}
