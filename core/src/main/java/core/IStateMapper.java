package core;

public interface IStateMapper<A, M> {
    M mapActionToState(A action);
}
