package ro.al.vi.cache.annotation.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import ro.al.vi.cache.annotation.MyCacheable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("ro.al.vi.cache.annotation.MyCacheable")
@AutoService(Processor.class) // TODO disable preprocessor execute in pom.xml and remove @AutoService(Processor.class)
public class MyCacheableProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, ClassEntry> classes = new HashMap<>();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MyCacheable.class)) {
            ExecutableElement executableElement = (ExecutableElement) annotatedElement;

            TypeMirror typeMirror = executableElement.getReturnType();
            // Кэшировать нечего, т.к. метод ничего не возвращает или количество входных аргументов не равно 1
            if (typeMirror instanceof NoType || typeMirror.getKind().equals(TypeKind.VOID) || executableElement.getParameters().size() != 1) {
                continue;
            }
            System.out.println("Return type " + executableElement.getReturnType() + " of method name " + executableElement.getSimpleName());
            VariableElement inParameter = executableElement.getParameters().getFirst();
            final Set<Modifier> modifiers = inParameter.getModifiers();
            String inParameterName = inParameter.getSimpleName().toString();
            System.out.println("inParameterName = " + inParameterName);
            String inParameterClassName = inParameter.asType().toString();
            System.out.println("inParameterClassName = " + inParameterClassName);
            String inParameterShortClassName = inParameterClassName.substring(inParameterClassName.lastIndexOf(".") + 1);
            String inParameterPackageClass = inParameterClassName.substring(0, inParameterClassName.lastIndexOf("."));
            String className = executableElement.getEnclosingElement().asType().toString();
            System.out.println("className = " + className);
            String shortClassName = className.substring(className.lastIndexOf(".") + 1);
            System.out.println("shortClassName = " + shortClassName);
            String packageClass = className.substring(0, className.lastIndexOf("."));
            System.out.println("packageClass = " + packageClass);
            String methodName = executableElement.getSimpleName().toString();
            System.out.println("methodName = " + methodName);
            String outParameterClassName = executableElement.getReturnType().toString();
            System.out.println("outParameterClassName = " + outParameterClassName);
            String outParameterShortClassName = outParameterClassName.substring(outParameterClassName.lastIndexOf(".") + 1);
            String outParameterPackageClass = outParameterClassName.substring(0, outParameterClassName.lastIndexOf("."));

            ClassName classNameKey = ClassName.get(inParameterPackageClass, inParameterShortClassName);
            ClassName classNameValue = ClassName.get(outParameterPackageClass, outParameterShortClassName);
            TypeName mapOfClassOfAnyAndClassOfAny = ParameterizedTypeName.get(ClassName.get(Map.class), classNameKey, classNameValue);
            TypeName hashMapOfClassOfAnyAndClassOfAny = null;
            if (executableElement.getAnnotation(MyCacheable.class).isThreadSafe()) {
                hashMapOfClassOfAnyAndClassOfAny = ParameterizedTypeName.get(ClassName.get(ConcurrentHashMap.class), classNameKey, classNameValue);
            } else {
                hashMapOfClassOfAnyAndClassOfAny = ParameterizedTypeName.get(ClassName.get(HashMap.class), classNameKey, classNameValue);
            }

            ClassEntry classEntry = classes.computeIfAbsent(className, ClassEntry::new);
            final String fieldName = toUpperSnakeCase(methodName + "CacheMap");
            FieldSpec cacheMapField = FieldSpec
                    .builder(mapOfClassOfAnyAndClassOfAny, fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new $T()", hashMapOfClassOfAnyAndClassOfAny)
                    .build();
            classEntry.addField(cacheMapField);
            MethodSpec cacheMethod = MethodSpec
                    .methodBuilder(methodName)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(classNameKey, inParameterName, Arrays.copyOf(modifiers.toArray(), modifiers.size(), Modifier[].class))
                    .beginControlFlow("if (" + fieldName + ".containsKey(" + inParameterName + "))")
                    .addStatement("return " + fieldName +".get(" + inParameterName + ")")
                    .endControlFlow()
                    .addStatement("$T result = super." + methodName +"(" + inParameterName + ")", classNameValue)
                    .addStatement(fieldName + ".put(" + inParameterName + ", result)")
                    .addStatement("return result")
                    .returns(classNameValue)
                    .build();
            classEntry.addMethod(cacheMethod);
            classEntry.setSuperClassName(shortClassName);
            classEntry.setPackageSuperClassName(packageClass);
        }

        for (Map.Entry<String, ClassEntry> entry : classes.entrySet()) {
            TypeSpec genClass = entry.getValue().build();
            JavaFile javaFile = JavaFile
                    .builder(entry.getValue().getPackageClassName(), genClass)
                    .indent("    ")
                    .build();
            try {
                File file = new File("target/generated-sources/cacheable");
                javaFile.writeTo(file);
//                javaFile.writeTo(System.out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

    private String toUpperSnakeCase(String s) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return s.replaceAll(regex, replacement).toUpperCase();
    }
}