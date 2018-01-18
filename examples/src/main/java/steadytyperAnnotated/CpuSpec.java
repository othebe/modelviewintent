package steadytyperAnnotated;

import processor.statemapper.annotations.ActionHandler;
import processor.statemapper.annotations.StateMapperSpec;
import rx.Observable;

@StateMapperSpec(
        modelClass = Integer.class,
        generatedClassName = "Cpu"
)
public class CpuSpec {
    @ActionHandler(KeyboardSpec.KEY_A)
    public Integer handleKeyA() {
        return 1;
    }

    @ActionHandler(KeyboardSpec.KEY_B)
    public Integer handleKeyB() {
        return 2;
    }
}
