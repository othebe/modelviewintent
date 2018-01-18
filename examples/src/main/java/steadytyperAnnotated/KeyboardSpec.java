package steadytyperAnnotated;

import processor.actiongenerator.annotations.ActionGeneratorSpec;

@ActionGeneratorSpec(
        actions = {
                KeyboardSpec.KEY_A,
                KeyboardSpec.KEY_B
        },
        generatedClassName = "Keyboard"
)
public class KeyboardSpec {
    public static final String KEY_A = "A";
    public static final String KEY_B = "B";
}
