package me.falu.twitchemotes.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.falu.twitchemotes.TwitchEmotes;
import org.apache.commons.lang3.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ConfigValue<T> {
    private final String key;
    private final T def;
    @Getter private T value = null;

    public ConfigValue(String key, T def) {
        this.key = key;
        this.def = def;
        this.initValue();
    }

    public void setValue(T newValue) {
        this.value = newValue;
        JsonObject config = ConfigFile.get();
        if (!(newValue instanceof List<?> list)) {
            Method addMethod = this.getAddMethod(newValue.getClass(), config);
            if (addMethod != null) {
                try {
                    addMethod.invoke(config, this.key, newValue);
                    ConfigFile.write(config);
                } catch (Exception ignored) {
                }
            }
        } else {
            JsonArray array = new JsonArray();
            if (!list.isEmpty()) {
                Class<?> type = list.get(0).getClass();
                for (Method method : JsonArray.class.getDeclaredMethods()) {
                    if (method.getName().equalsIgnoreCase("add")) {
                        if (method.getParameterCount() > 0 && method.getParameterTypes()[0].isAssignableFrom(type)) {
                            for (Object value : list) {
                                try {
                                    method.invoke(array, value);
                                } catch (Exception ignored) {
                                }
                            }
                            break;
                        }
                    }
                }
            }
            config.add(this.key, array);
            ConfigFile.write(config);
        }
    }

    @SuppressWarnings("unchecked")
    private void initValue() {
        JsonElement element = this.getElement();

        Class<?> type = this.def.getClass();
        if (!(this.def instanceof List)) {
            T temp = null;
            if (element == null || element.isJsonNull()) {
                this.setValue(this.def);
            } else {
                Method elementMethod = this.getElementMethod(type, element);
                if (elementMethod != null) {
                    try {
                        temp = (T) elementMethod.invoke(element);
                    } catch (Exception ignored) {
                    }
                }
            }
            if (temp != null) {
                this.value = temp;
            }
        } else {
            if (element == null || element.isJsonNull()) {
                this.setValue(this.def);
                return;
            }
            try {
                List<?> values = (List<?>) this.def;
                if (!values.isEmpty()) {
                    type = values.get(0).getClass();
                    ArrayList<Object> list = new ArrayList<>();
                    for (JsonElement element1 : element.getAsJsonArray()) {
                        Method elementMethod = this.getElementMethod(type, element1);
                        if (elementMethod != null) {
                            try {
                                list.add(elementMethod.invoke(element1));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    this.value = (T) list;
                } else {
                    this.value = (T) new ArrayList<>();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Class<?> getPrimitiveType(Class<?> type) {
        try {
            if (ClassUtils.getAllInterfaces(type).contains(Serializable.class)) {
                Field field = type.getDeclaredField("value");
                return field.getType();
            }
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            TwitchEmotes.LOGGER.error("While getting primitive type (" + this.key + ")", e);
        }
        return null;
    }

    private Method getElementMethod(Class<?> type, JsonElement element) {
        for (Method method : JsonElement.class.getDeclaredMethods()) {
            if (method.getName().startsWith("getAs") && method.canAccess(element) && method.getParameterCount() == 0) {
                if (method.getReturnType().equals(type) || method.getReturnType().equals(this.getPrimitiveType(type))) {
                    return method;
                }
            }
        }
        return null;
    }

    private Method getAddMethod(Class<?> type, JsonObject object) {
        for (Method method : JsonObject.class.getDeclaredMethods()) {
            if (method.getName().equalsIgnoreCase("addProperty")) {
                if (method.canAccess(object) && method.getParameterCount() >= 2) {
                    if (method.getParameterTypes()[1].isAssignableFrom(type)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    public T getDefault() {
        return this.def;
    }

    @SuppressWarnings("unused")
    public boolean isDefault() {
        return !this.getDefault().equals(this.value);
    }

    private JsonElement getElement() {
        JsonObject config = ConfigFile.get();
        return config.get(this.key);
    }
}
