package kushan.reactive.test.example_test.flux_processor;

import reactor.core.publisher.Flux;

public class FluxTest1 {
    final FluxProcessor<Integer> fluxProcessor = new FluxProcessor<>("kush");
    final FluxProcessor<Integer> fluxProcessor2 = new FluxProcessor<>("kush1");


    public static void main(String[] args) throws InterruptedException {
        FluxTest1 test = new FluxTest1();
        Flux<Integer> flux = Flux.concat(Flux.just(10,10),test.fluxProcessor.flux().doOnNext(j -> System.out.println("ss")));

        // flux.map(i -> i * 2);
        flux.subscribe(j -> print(j));
        int i = 0;
        while (i != 5) {
            Thread.sleep(1000);
            i++;
            test.sendValuesFlux1(i);
            if(i == 3){

            }
            Thread.sleep(1000);
        }
    }

    private static void print(Integer j) {
        System.out.println(j);
    }

    public void sendValuesFlux1(int value) {
        fluxProcessor.send(value);
    }

    public void sendValuesFlux2(int value) {
        fluxProcessor2.send(value*10);
    }
}

