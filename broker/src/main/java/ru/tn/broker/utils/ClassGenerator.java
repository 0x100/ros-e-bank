package ru.tn.broker.utils;

import com.google.common.base.CaseFormat;
import javassist.ClassPool;
import javassist.CtClass;
import lombok.SneakyThrows;

public class ClassGenerator {

    @SneakyThrows
    public static <T> Class getNewFeignClientInterface(String name, Class<T> oldFeignClientInterface) {
        ClassPool classPool = ClassPool.getDefault();
        CtClass paymentFeignClient = classPool.get(oldFeignClientInterface.getName());
        paymentFeignClient.defrost();
        paymentFeignClient.setName(beanNameToCamelCase(name));
        paymentFeignClient.setSuperclass(classPool.get(oldFeignClientInterface.getName()));

        return paymentFeignClient.toClass();
    }

    public static String beanNameToCamelCase(String name) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
    }
}
