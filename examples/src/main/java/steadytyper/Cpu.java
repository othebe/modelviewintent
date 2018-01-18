package steadytyper;

import core.IActionGenerator;
import core.IModelRenderer;
import core.IStateMapper;

public class Cpu implements IStateMapper<Integer, String> {

    public Cpu() { }

    @Override
    public String mapActionToState(Integer action) {
        return Character.toString((char) action.intValue());
    }

    public void attachActionGenerator(IActionGenerator<Integer> actionGenerator, IModelRenderer<String> modelRenderer) {
        actionGenerator.getActionObservable()
                .map(action -> mapActionToState(action))
                .subscribe(
                        state -> modelRenderer.render(state),
                        throwable -> { throw new RuntimeException(throwable); });
    }

    public void dropActionGenerator() {
    }
}
