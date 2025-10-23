package net.rptools.extra.addon.wrappers;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AProxy<T> {
    private static final Logger log = LogManager.getLogger(AProxy.class);

    public final Map<String, Method> methods = new HashMap<>();
    public final Map<String, Field> fields = new HashMap<>();
    public final Map<String, Object> constants = new HashMap<>();
    public final Map<String, Enum<?>> enums = new HashMap<>();
    public final Map<String, Record> records = new HashMap<>();
    private T proxyInstance;
    private final Class<T> klass;

    public AProxy(Class<T> klass) {
        this.klass = klass;
        init();
    }

    private void init() {
        for (Method method : klass.getDeclaredMethods()) {
            this.methods.put(method.getName(), method);
        }
//        FieldAccessor
        List<String> methodListLC = methods.keySet().stream().map(String::toLowerCase).toList();
        for (Field field : FieldUtils.getAllFieldsList(klass)) {
            try {
                String fieldName = field.getName();
                if (field.getType().isRecord() || field.getType().isEnum() || fieldName.equals(fieldName.toUpperCase())) {
                    Object value = new Object();
                    try {
                        FieldUtils.readField(value, fieldName, true);
                    } catch (NullPointerException ignored){
                        value = FieldUtils.readStaticField(field.getType(), fieldName, true);
                    } catch (Exception npe){
                        value = null;
                    }
                    constants.put(fieldName, value);
                } else {
                    this.fields.put(fieldName, field);
                    List<String> likelyGetterNames = List.of("get" + fieldName, "is" + fieldName, "has" + fieldName);
                    String likelySetter = "set" + fieldName;
                    boolean noSetter = !methodListLC.contains(likelySetter);
                    boolean noGetter = likelyGetterNames.stream().filter(methodListLC::contains).toList().isEmpty();
                    if (field.getType().isEnum()) {
                        Class<?> classForEnum = Class.forName(fieldName);
                        this.enums.put(fieldName, enumGetter(field));
                        this.constants.put(fieldName, EnumUtils.getEnumList((Class<Enum>) Class.forName(fieldName)));
                        for (Method m : classForEnum.getDeclaredMethods()) {
                            this.methods.put(MessageFormat.format("{0}.{1}", fieldName, m.getName()), m);
                        }
                    }
//                    else if (field.getMediaType().isRecord()) {
//                        this.records.put(fieldName, field.);
//                    }
                }
            } catch (IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        proxyInstance = (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class[]{klass}, new DynamicInvocationHandler());
    }

    private Enum<?> enumGetter(Field field) {
        return getEnum(field.getType().getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked", "RegExpRedundantEscape"})
    static private Enum<?> getEnum(String enumFullName) {
        String[] x = enumFullName.split("\\.(?=[^\\.]+$)");
        if (x.length == 2) {
            String enumClassName = x[0];
            String enumName = x[1];
            try {
                @SuppressWarnings("unchecked")
                Class<Enum> cl = (Class<Enum>) Class.forName(enumClassName);
                return Enum.valueOf(cl, enumName);
            } catch (ClassNotFoundException e) {
                log.info(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }


    @SuppressWarnings({"TypeParameterExplicitlyExtendsObject", "unchecked", "TypeParameterHidesVisibleType"})
    private <T extends Object> T getter(Field field) throws IllegalAccessException {
        Object result = new Object();
        FieldUtils.readField(field, result, true);
        return (T) result;
    }

    private void setter(Field field, Object value) throws IllegalAccessException {
        FieldUtils.writeField(field, proxyInstance, value, true);
    }

    private static class DynamicInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.info("Invoked method: {}", method.getName());
            return 42;
        }
    }
}
