package steadytyperAnnotated;

import processor.modelrenderer.annotations.ModelRendererSpec;

@ModelRendererSpec(
        modelClass = Integer.class,
        generatedClassName = "Monitor"
)
public class MonitorSpec {
    public void render(Integer state) {
        System.out.printf("%s: %d\n", this.toString(), state);
    }
}
