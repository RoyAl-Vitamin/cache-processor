package ro.al.vi.cache.annotation.processor;

import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClassEntry {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final List<String> GENERATED_VALUES = List.of("MyCache version:1.0-SNAPSHOT");

    public ClassEntry(String className) {
        this.className = className.substring(className.lastIndexOf(".") + 1);
        this.packageClassName = className.substring(0, className.lastIndexOf("."));
        this.modifiers = new ArrayList<>() {{
            add(Modifier.PUBLIC);
        }};
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    private String packageClassName;

    private String className;

    private String packageSuperClassName;

    private String superClassName;

    private List<Modifier> modifiers;

    private List<FieldSpec> fields;

    private List<MethodSpec> methods;

    public void addField(FieldSpec fieldSpec) {
        this.fields.add(fieldSpec);
    }

    public void addMethod(MethodSpec methodSpec) {
        this.methods.add(methodSpec);
    }

    public TypeSpec build() {
        ClassName superClassName = ClassName.get(this.packageSuperClassName, this.superClassName);
        return TypeSpec
                .classBuilder("MyCacheable" + this.className)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value",
                                "$L",
                                GENERATED_VALUES.stream()
                                        .map(value -> CodeBlock.of("\"$L\"", value))
                                        .collect(CodeBlock.joining(",", "{", "}")))
                        .addMember("date", "$S", ZonedDateTime.now().format(FORMATTER))
                        .addMember("comments", "$S", "This class is generated by CacheProcessor")
                        .build())
                .superclass(superClassName)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fields)
                .addMethods(methods)
                .build();
    }

    public String getPackageClassName() {
        return packageClassName;
    }

    public void setPackageClassName(String packageClassName) {
        this.packageClassName = packageClassName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageSuperClassName() {
        return packageSuperClassName;
    }

    public void setPackageSuperClassName(String packageSuperClassName) {
        this.packageSuperClassName = packageSuperClassName;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public List<FieldSpec> getFields() {
        return fields;
    }

    public void setFields(List<FieldSpec> fields) {
        this.fields = fields;
    }

    public List<MethodSpec> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodSpec> methods) {
        this.methods = methods;
    }
}