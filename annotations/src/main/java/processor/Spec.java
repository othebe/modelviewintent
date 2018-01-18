package processor;

import com.squareup.javapoet.*;

import javax.lang.model.element.PackageElement;
import java.util.*;

public class Spec {
    private final String packageName;
    private final String className;

    private final List<TypeName> superInterfaces;
    private final List<FieldSpec> fields;
    private final List<MethodSpec> methods;

    public Spec(final PackageElement packageElement, final String className) {
        this.packageName = packageElement.getQualifiedName().toString();
        this.className = className;

        this.superInterfaces = new LinkedList<>();
        this.fields = new LinkedList<>();
        this.methods = new LinkedList<>();
    }

    public void add(TypeName superInterface) {
        superInterfaces.add(superInterface);
    }

    public void add(FieldSpec fieldSpec) {
        fields.add(fieldSpec);
    }

    public void add(MethodSpec methodSpec) {
        methods.add(methodSpec);
    }

    public JavaFile buildJavaFile() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className);

        for (TypeName superInterface : superInterfaces) {
            builder.addSuperinterface(superInterface);
        }

        for (FieldSpec fieldSpec : fields) {
            builder.addField(fieldSpec);
        }

        for (MethodSpec methodSpec : methods) {
            builder.addMethod(methodSpec);
        }

        return JavaFile.builder(packageName, builder.build()).build();
    }
}
