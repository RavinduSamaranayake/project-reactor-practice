package kushan.reactive.test.example_test.schedular_test;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ReactorSchedulerTest {

    @Test
    public void basicThreadTest() {
        Flux<Integer> flux = Flux.range(0, 2)
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                });
        printValues(flux);
    }

    @Test
    public void publishOnImmediateTest() {
        Flux<Integer> flux = Flux.range(0, 2)
                .publishOn(Schedulers.immediate())
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                });
        printValues(flux);
    }

    @Test
    public void publishOnSingleTest() {
        Flux<Integer> flux = Flux.range(0, 2)
                .publishOn(Schedulers.single())
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                });
        printValues(flux);
    }

    @Test
    public void publishOnNewSingleTest() {
        Flux<Integer> flux = Flux.range(0, 2)
                .publishOn(Schedulers.newSingle("my-custom"))
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                });
        printValues(flux);
    }

    @Test
    public void publishOnParallelTest() {
        Flux<Integer> flux = Flux.range(0, 5)
                .publishOn(Schedulers.parallel())
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                });
        printValues(flux);
    }

    @Test
    public void multiplePublishOnTest() {
        Flux<Integer> flux = Flux.range(0, 2)
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                }).publishOn(Schedulers.single())
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                }).publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                }).publishOn(Schedulers.parallel())
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                });
        printValues(flux);
    }


    public void printValues(Flux<Integer> flux){
        //create a runnable with flux subscription
        Runnable r = () -> flux.subscribe(s -> {
            System.out.println("Received " + s + " via " + Thread.currentThread().getName());
        });

        //normal subscribe
        flux.subscribe(s -> System.out.println("Received " + s + " via " + Thread.currentThread().getName()));

        Thread t1 = new Thread(r, "t1");
        Thread t2 = new Thread(r, "t2");
        Thread t3 = new Thread(r, "t3");

        //lets start the threads. (this is when we are subscribing to the flux)
        System.out.println("Program thread :: " + Thread.currentThread().getName());
        t1.start();
        t2.start();
        t3.start();
    }

    @Test
    public void subscribeOnTest() {
        Flux<Integer> flux = Flux.range(0, 2)
                .map(i -> {
                    System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
                    return i;
                });

        //create a runnable with flux subscription
        Runnable r = () -> flux.subscribeOn(Schedulers.parallel()).subscribe(s -> {
            System.out.println("Received " + s + " via " + Thread.currentThread().getName());
        });

        //normal subscribe
        flux.subscribeOn(Schedulers.parallel()).subscribe(s -> System.out.println("Received " + s + " via " + Thread.currentThread().getName()));

        Thread t1 = new Thread(r, "t1");
        Thread t2 = new Thread(r, "t2");
        Thread t3 = new Thread(r, "t3");

        //lets start the threads. (this is when we are subscribing to the flux)
        System.out.println("Program thread :: " + Thread.currentThread().getName());
        t1.start();
        t2.start();
        t3.start();
    }
}
