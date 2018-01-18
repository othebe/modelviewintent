package steadytyper;

import core.IActionGenerator;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class Keyboard implements IActionGenerator<Integer> {
    private Func1<Integer, Void> emitActionFunc;

    private Observable<Integer> actionObservable = Observable.create(new Action1<Emitter<Integer>>() {
        @Override
        public void call(Emitter<Integer> emitter) {
            emitActionFunc = new Func1<Integer, Void>() {
                @Override
                public Void call(Integer action) {
                    emitter.onNext(action);
                    return null;
                }
            };
        }
    }, Emitter.BackpressureMode.NONE)
            .doOnUnsubscribe(new Action0() {
                @Override
                public void call() {
                    emitActionFunc = null;
                }
    }).share();

    public void triggerAction(Integer action) {
        if (emitActionFunc != null) {
            emitActionFunc.call(action);
        }
    }

    @Override
    public Observable<Integer> getActionObservable() {
        return actionObservable;
    }
}
