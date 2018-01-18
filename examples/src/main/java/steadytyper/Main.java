package steadytyper;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Keyboard keyboard = new Keyboard();
        Monitor monitor = new Monitor();
        Cpu cpu = new Cpu();
        cpu.attachActionGenerator(keyboard, monitor);
        cpu.attachActionGenerator(keyboard, new Monitor());

        Integer[] actions = { 72, 69, 76, 76, 79, 32, 87, 79, 82, 76, 68, 33 };

        Observable
                .zip(
                    Observable.interval(300, TimeUnit.MILLISECONDS),
                    Observable.from(actions),
                    (ignore, action) -> action)
                .toBlocking()
                .subscribe(action -> keyboard.triggerAction(action));
    }
}
