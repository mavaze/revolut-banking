package io.github.mavaze.revolut.corebank.eventing;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class EventPublisher {

    private static List<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();

//    static {
//        Reflections reflections = new Reflections("io.github.mavaze.revolut.corebank");
//        Set<Class<? extends MyInterface>> classes = reflections.getSubTypesOf(MyInterface.class);
//    }

    public void subscribe(EventSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(BankingEvent event) {
        subscribers.stream()
                .filter(sub -> sub.subscribedFor(event))
                .forEach(sub -> sub.submit(event));
    }
}
