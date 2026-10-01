package com.example.test_reactive_spring.controller;

import com.hazelcast.jet.json.JsonUtil;
import reactor.core.Disposable;
import reactor.core.publisher.*;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


enum STATUS {
    APPROVED(0),
    REJECTED(1),
    RESEND(2),
    DRAFT(3);

    int value = Integer.MIN_VALUE;

    STATUS(int value) {
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }
}

record Student(UUID uetr,String firstName,String lastName,int rank){

}

public class ReactiveTest {

    public static void main(String[] args) throws Exception {
        ReactiveTest reactiveTest = new ReactiveTest();
//        Mono<String> monoString = Mono.just("satheesh").log();
//        monoString.subscribe(System.out::println);
//
//        Mono<?> monoString2 = Mono.just("sekar").then(
//                Mono.error(new RuntimeException("Mono Manual Error Thrown By Satheesh"))
//        ).log();
//        monoString2.subscribe(System.out::println,e -> System.out.println(e.getMessage()));
//
//        Flux<String> fluxString = Flux.just("satheesh","sekar","kannan","mari")
//                .concatWithValues("babuji")
//                .concatWith(Flux.error(new RuntimeException("Flux Manual Error Thrown By Satheesh")))
//                .concatWithValues("hari")
//                .log();
//
//        fluxString.subscribe(System.out::println);

//        System.out.println(Runtime.getRuntime().availableProcessors());
//        System.out.println(Runtime.getRuntime().totalMemory()/1024);
//        System.out.println(Runtime.getRuntime().freeMemory()/1024);
//        System.out.println(Runtime.getRuntime().maxMemory()/1024);

//        Flux<Map<String,Object>> anyFlux = Flux.just(Map.of("name","satheesh"),Map.of("name","kannan"));
//        anyFlux.flatMap(data-> Mono.just(data))
//                .subscribe(data-> System.out.println(data));

//        System.out.println(STATUS.APPROVED);
//        System.out.println(STATUS.APPROVED.getValue());

//        try {
//            InetAddress localHost = InetAddress.getLocalHost();
//            System.out.println(localHost.getHostAddress());
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }

//        Flux<Student> studentFlux = Flux.just(
//                new Student(UUID.randomUUID(),"satheesh","d",10),
//                new Student(UUID.randomUUID(),"kannan","m",1),
//                new Student(UUID.randomUUID(),"mari","c",5),
//                new Student(UUID.randomUUID(),"sekar","s",2)
//        );

//        Mono<Map<UUID,List<Student>>> monoMap = studentFlux.groupBy(key->key.uetr())
//                        .flatMap(group->group.collectList().map(list->Map.entry(group.key(),list)))
//                                .collectMap(Map.Entry::getKey,Map.Entry::getValue);
//        monoMap.subscribe(System.out::println);
//
//        Mono<Map<UUID,Student>> monoData = studentFlux.collectMap(Student::uetr,student -> student);
//        monoData.subscribe(System.out::println);

//        System.out.println(UUID.randomUUID());
//        reactiveTest.exceptionDemo().subscribe(System.out::println);
//        Thread.sleep(21_000);

        reactiveTest.buildQuery().subscribe(data-> System.out.println(data));
    }

    private Mono<String> monoString(){
        return Mono.just("Reactive");
    }

    private Flux<String> fluxString(){
        Flux<String> flux = Flux.just("satheesh","developer","india","chennai");
        return flux.delayElements(Duration.ofSeconds(2)).flatMap(data-> Mono.just(data.toUpperCase()));
    }

    private Flux<Integer> concatFlux(){
        Flux<Integer> flux1 = Flux.range(1,20).delayElements(Duration.ofMillis(500));
        Flux<Integer> flux2 = Flux.range(101,20).delayElements(Duration.ofMillis(500));
        return Flux.concat(flux1,flux2);
    }


    private Mono<List<Integer>> skipDemo(){
        Flux<Integer> flux =  Flux.range(1,20);
        return flux.collectList();
    }

    private Flux<Integer> mergeDemo(){
        Flux<Integer> flux1 =  Flux.range(1,20).delayElements(Duration.ofMillis(500));
        Flux<Integer> flux2 =  Flux.range(101,20).delayElements(Duration.ofMillis(500));
        return Flux.merge(flux1,flux2);
    }

    private Flux<Tuple3<Integer,Integer,String>> zipDemo(){
        Flux<Integer> flux1 =  Flux.range(1,20).delayElements(Duration.ofMillis(500));
        Flux<Integer> flux2 =  Flux.range(101,20).delayElements(Duration.ofMillis(500));
        Mono<String> mono =  Mono.just("satheesh");
        return Flux.zip(flux1,flux2,mono);
    }

    private Flux<List<Integer>> bufferDemo(){
        Flux<Integer> flux = Flux.range(1,20).delayElements(Duration.ofSeconds(1));
        return flux.buffer(5);
    }

    private Flux<Long> over(){
        Flux<Long> flux = Flux.range(1,Integer.MAX_VALUE).log().concatMap(data-> Mono.delay(Duration.ofMillis(100)));
        return flux;
    }

    private Flux<Long> overFlow(){
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureDrop()
                .concatMap(data-> Mono.delay(Duration.ofMillis(100)).thenReturn(data))
                .doOnNext(data-> System.out.println(data));
    }

    private Flux<Long> backPressure(){
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureBuffer(50, BufferOverflowStrategy.DROP_OLDEST)
                .concatMap(data-> Mono.delay(Duration.ofMillis(100)).thenReturn(data))
                .doOnNext(data-> System.out.println(data));
    }

    private Mono<Map<Integer,Integer>> mapDemo(){
        Flux<Integer> flux =  Flux.range(1,20);
        return flux.collectMap(key-> key,value-> value * value);
    }

    private Flux<Integer> completeDemo(){
        Flux<Integer> flux =  Flux.range(1,20);
        return flux.doOnComplete(()-> System.out.println("Data Done"));
    }

    private Flux<Integer> eachDemo(){
        Flux<Integer> flux =  Flux.range(1,20);
        return flux.doOnEach(signal -> {
            if(signal.getType() == SignalType.ON_COMPLETE){
                System.out.println("Data Done");
            } else {
                System.out.println(signal.get() +" "+ signal.getType());
            }
        });
    }

    private Flux<Integer> doNextDemo(){
        Flux<Integer> flux =  Flux.range(1,20);
        return flux.doOnNext(data->{
            System.out.println(data);
        });
    }

    private Flux<Integer> subscribeDemo(){
        Flux<Integer> flux =  Flux.range(1,20).delayElements(Duration.ofSeconds(1));
        return flux
                .doOnSubscribe(data-> System.out.println("Subscribed:"+data))
                .doOnNext(System.out::println)
                .doOnCancel(()-> System.out.println("Subscription Canceled"));
    }

    private Flux<Integer> exceptionDemo() throws Exception {
        Flux<Integer> flux =  Flux.range(1,20).delayElements(Duration.ofSeconds(1));
        Flux<Integer> splFlux = Flux.range(101,20);
        return flux.map(data->{
            if(data == 5){
                throw new RuntimeException("Manual Exception");
            }
            return data;
        }).onErrorResume(throwable -> splFlux);
//                .onErrorReturn( RuntimeException.class,50);
//                .onErrorContinue(((throwable, o) -> System.out.println("Do Nothing..")));
    }


    private Mono<String> buildQuery(){
        return Flux.range(1, 100)
                .map(i -> String.format("emp_id emp_id%d,first_name first_name%d,last_name last_name%d,ctry_cd ctry_cd%d", i, i, i, i))
                .collect(Collectors.joining("," + System.lineSeparator()));
    }
}
