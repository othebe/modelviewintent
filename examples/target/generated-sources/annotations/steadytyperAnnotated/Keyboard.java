package steadytyperAnnotated;

import core.IActionGenerator;
import java.lang.Override;
import java.lang.String;
import java.lang.Void;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

class Keyboard implements IActionGenerator<String> {
  private KeyboardSpec base = new KeyboardSpec();

  private Func1<String, Void> emitActionFunc;

  private final Observable<String> actionObservable = Observable.create(new Action1<Emitter<String>>() {
    @Override
    public void call(Emitter<String> emitter) {
      emitActionFunc = new Func1<String, Void>() {
        @Override
        public Void call(String action) {
          emitter.onNext(action);
          return null;
        }
      };
    }
  }, Emitter.BackpressureMode.NONE).doOnUnsubscribe(new Action0() {
    @Override
    public void call() {
      emitActionFunc = null;
    }
  }).share();

  @Override
  public Observable<String> getActionObservable() {
    return actionObservable;
  }

  public void triggerAction(String action) {
    if (emitActionFunc != null) {
      emitActionFunc.call(action);
    }
  }
}
