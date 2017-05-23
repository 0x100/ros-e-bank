package ru.tn.broker.utils;

import com.google.common.base.CaseFormat;
import lombok.SneakyThrows;
import org.springframework.cglib.core.ReflectUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.springframework.objenesis.instantiator.basic.ClassDefinitionUtils.*;

@SuppressWarnings("unchecked")
public class ClassGenerator {
    private static final List<String> SKIP_METHODS = Arrays.asList("equals", "hashCode", "toString");

    public <T> Class getProxyClass(String name, Class<T> feignClientClass) throws Exception {
        String className = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
        byte[] classData = generateClassData(feignClientClass, className);
        return ReflectUtils.defineClass(className, classData, getClass().getClassLoader());
    }

    private <T> byte[] generateClassData(Class<T> feignClientClass, String className) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        String superClassName = "java/lang/reflect/Proxy";
        Class<sun.misc.ProxyGenerator> proxyGeneratorClass = sun.misc.ProxyGenerator.class;

        Constructor<?> constructor = proxyGeneratorClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        sun.misc.ProxyGenerator generator = (sun.misc.ProxyGenerator) constructor.newInstance(className, new Class[]{feignClientClass}, 0);

        Method generateClassFileMethod = proxyGeneratorClass.getDeclaredMethod("generateClassFile");
        generateClassFileMethod.setAccessible(true);
        generateClassFileMethod.invoke(generator);

        Field interfacesField = proxyGeneratorClass.getDeclaredField("interfaces");
        interfacesField.setAccessible(true);

        Field fieldsField = proxyGeneratorClass.getDeclaredField("fields");
        fieldsField.setAccessible(true);

        Field methodsField = proxyGeneratorClass.getDeclaredField("methods");
        methodsField.setAccessible(true);

        Class[] interfaces = (Class[]) getFieldValue(interfacesField, generator);
        List fields = (List) getFieldValue(fieldsField, generator);
        List methods = (List) getFieldValue(methodsField, generator);

        Class<?> c = methods.get(0).getClass();
        Field nameField = c.getDeclaredField("name");
        nameField.setAccessible(true);

        methods.removeIf(method -> SKIP_METHODS.contains(getFieldValue(nameField, method).toString()));

        Field constantPoolField = proxyGeneratorClass.getDeclaredField("cp");
        constantPoolField.setAccessible(true);

        Object constantPool = getFieldValue(constantPoolField, generator);
        Class<?> constantPoolClass = constantPool.getClass();

        Method getClassMethod = constantPoolClass.getDeclaredMethod("getClass", String.class);
        getClassMethod.setAccessible(true);

        Method constantPoolWriteMethod = constantPoolClass.getDeclaredMethod("write", OutputStream.class);
        constantPoolWriteMethod.setAccessible(true);

        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(ba);

        try {
            os.writeInt(0xCAFEBABE);
            os.writeShort(0);
            os.writeShort(49);
            constantPoolWriteMethod.invoke(constantPool, os);
            os.writeShort(ACC_PUBLIC | ACC_FINAL | ACC_SUPER);
            os.writeShort((short) getClassMethod.invoke(constantPool, className.replace('.', '/')));
            os.writeShort((short) getClassMethod.invoke(constantPool, superClassName));
            os.writeShort(interfaces.length);
            for (Class i : interfaces) {
                os.writeShort((short) getClassMethod.invoke(constantPool, i.getName().replace('.', '/')));
            }
            os.writeShort(fields.size());
            for (Object f : fields) {
                Method writeMethod = f.getClass().getDeclaredMethod("write", DataOutputStream.class);
                writeMethod.setAccessible(true);
                writeMethod.invoke(f, os);
            }
            os.writeShort(methods.size());
            for (Object m : methods) {
                Method writeMethod = m.getClass().getDeclaredMethod("write", DataOutputStream.class);
                writeMethod.setAccessible(true);
                writeMethod.invoke(m, os);
            }
            os.writeShort(0);
        } catch (IOException e) {
            throw new InternalError("Unexpected I/O Exception");
        }
        return ba.toByteArray();
    }

    @SneakyThrows
    private Object getFieldValue(Field nameField, Object it) {
        return nameField.get(it);
    }
}
