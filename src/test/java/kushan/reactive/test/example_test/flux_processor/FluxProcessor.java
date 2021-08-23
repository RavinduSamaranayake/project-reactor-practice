package kushan.reactive.test.example_test.flux_processor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class FluxProcessor<T> {
    private final List<FluxSink<T>> emitters;
    private final Flux<T> flux;
    private final String name;

    public FluxProcessor(String name) {
        this(name, (em) -> {
        });
    }

    public FluxProcessor(String name, Consumer<FluxSink<T>> cb) {
        this.emitters = new CopyOnWriteArrayList();
        this.name = name;
        this.flux = Flux.create((emitter) -> {
            this.emitters.add(emitter);
            cb.accept(emitter);
            emitter.onCancel(() -> {
                this.removeEmitter(emitter);
            });
            emitter.onDispose(() -> {
                this.removeEmitter(emitter);
            });
        });
    }

    public String getName() {
        return this.name;
    }

    public Flux<T> flux() {
        return this.flux;
    }

    public void error(Throwable thr) {
        this.emitters.forEach((emitter) -> {
            try {
                emitter.error(thr);
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        });
    }

    public void send(T data) {
        this.emitters.forEach((emitter) -> {
            try {
                emitter.next(data);
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        });
    }

    public void complete() {
        this.emitters.forEach((emitter) -> {
            try {
                emitter.complete();
            } catch (Exception var2) {
                var2.printStackTrace();
            }

        });
    }

    private void removeEmitter(FluxSink<T> sink) {
        this.emitters.remove(sink);
    }

    public int getActiveSubscribers() {
        return this.emitters.size();
    }
}