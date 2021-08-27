package kushan.reactive.test.example_test.user_example;

import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.util.List;
import java.util.stream.Stream;

public class ActivityCheck {
    public static void main(String[] args) {
        Flux<List<User>> listFlux = Flux.just(new User("123","kamal","des1"),new User("123","amali","des2")
                ,new User("123","malith","des3"),new User("1123","kushan","des4"),new User("123","janith","des5"),
                new User("12883","rathan","des6"),new User("12300","mahesh","des7")).bufferUntilChanged(User::getId);

       // listFlux.subscribe(System.out::println); we can use reactive stream multiple
        listFlux.subscribe(users -> System.out.println(users),err -> System.out.println(err),() -> run(),subscription -> subscription.request(5));

        listFlux.subscribe(users -> System.out.println(users),err -> System.out.println(err),() -> run(),subscription -> subscription.request(3));

        System.out.println("--------------------test Stream--------------------------");

        Stream<List<User>> testStream = listFlux.toStream();

        testStream.forEach(System.out::println);

        System.out.println("--------------------test Stream usage--------------------------"); //but stream can use only one time

        //testStream.forEach(System.out::println);

        Flux<User> flux1 = Flux.just(new User("123","kamal","des1"),new User("123","amali","des2")
                ,new User("123","malith","des3"),new User("1123","kushan","des4"),new User("123","janith","des5"),
                new User("12883","rathan","des6"),new User("12300","mahesh","des7"))
                .thenMany(Flux.just(new User("123","kamal","des11"),new User("123","amali","des21"))).doOnNext(user -> System.out.println("Print user : "+user));
        flux1.take(3);

        flux1.subscribe(System.out::println);

    }

    private static void run() {
        System.out.println("Complete");
    }
}
