package steadytyperAnnotated;

import core.IActionGenerator;
import java.lang.Integer;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.Throwable;
import java.util.HashMap;
import java.util.Map;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

class SteadyTyperCoordinator {
  private Map<IActionGenerator, Subscription> subscriptionMap = new HashMap();

  public static Subscription listen(Keyboard actionGenerator, Cpu stateMapper,
      Monitor modelRenderer) {
    Subscription subscription = actionGenerator.getActionObservable().map(new Func1<String, Integer>() {
      @Override
      public Integer call(String action) {
        return stateMapper.mapActionToState(action);
      }
    }).subscribe(new Action1<Integer>() {
      @Override
      public void call(Integer state) {
        modelRenderer.render(state);
      }
    }, new Action1<Throwable>() {
      @Override
      public void call(Throwable throwable) {
        throw new RuntimeException(throwable);
      }
    });
    return subscription;
  }
}
