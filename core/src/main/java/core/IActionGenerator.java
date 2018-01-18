package core;

import rx.Observable;

public interface IActionGenerator<A> {
    Observable<A> getActionObservable();
}
