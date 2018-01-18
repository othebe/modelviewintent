package steadytyper;

import core.IModelRenderer;

public class Monitor implements IModelRenderer<String> {
    @Override
    public void render(String state) {
        System.out.printf("%s", state);
    }
}
