package steadytyperAnnotated;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Keyboard keyboard = new Keyboard();
        Monitor monitor1 = new Monitor();
        Monitor monitor2 = new Monitor();
        Cpu cpu = new Cpu();

        SteadyTyperCoordinator.listen(keyboard, cpu, monitor1);
        SteadyTyperCoordinator.listen(keyboard, cpu, monitor2);

        String[] actions = { KeyboardSpec.KEY_A, KeyboardSpec.KEY_B, KeyboardSpec.KEY_A, KeyboardSpec.KEY_B };


        // Simulate key presses on keyboard.
        Observable
                .zip(
                        Observable.interval(300, TimeUnit.MILLISECONDS),
                        Observable.from(actions),
                        (ignore, action) -> action)
                .toBlocking()
                .subscribe(action -> keyboard.triggerAction(action));
    }
}
