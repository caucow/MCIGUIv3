package com.caucraft.mciguiv3.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonConfig {

    private static final BiFunction<JsonElement, Object, JsonElement> ARRAY_FUNC = new BiFunction<JsonElement, Object, JsonElement>() {

        private final int hash = "A_F".hashCode();

        @Override
        public JsonElement apply(JsonElement json, Object i) {
            try {
                return json.getAsJsonArray().get((Integer) i);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return "A_F";
        }

        @Override
        public int hashCode() {
            return hash;
        }
    };
    private static final BiFunction<JsonElement, Object, JsonElement> MAP_FUNC = new BiFunction<JsonElement, Object, JsonElement>() {

        private final int hash = "M_F".hashCode();

        @Override
        public JsonElement apply(JsonElement json, Object s) {
            try {
                return json.getAsJsonObject().get((String) s);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return "M_F";
        }

        @Override
        public int hashCode() {
            return hash;
        }
    };
    private static final Map<String, JsonAccessor> accessorMap = new HashMap<>();
    private static final Map<String, List<BiFunction<JsonElement, Object, JsonElement>>> accessFunctionMap = new HashMap<>();
    protected static final Gson gson;

    static {
        accessorMap.put("", new JsonAccessor(
                new ArrayList<BiFunction<JsonElement, Object, JsonElement>>(0),
                new ArrayList<Object>(0)));
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create();
    }

    private static Object unJson(JsonElement e) {
        if (e instanceof JsonObject) {
            return new MapWrapper(e.getAsJsonObject());
        } else if (e instanceof JsonArray) {
            return new ListWrapper(e.getAsJsonArray());
        } else if (e instanceof JsonPrimitive) {
            JsonPrimitive prim = e.getAsJsonPrimitive();
            if (prim.isBoolean()) {
                return prim.getAsBoolean();
            } else if (prim.isNumber()) {
                return prim.getAsNumber();
            } else if (prim.isString()) {
                return prim.getAsString();
            }
        }
        return null;
    }

    protected JsonElement root;

    public JsonConfig() {
        this(new JsonObject());
    }

    public JsonConfig(JsonElement e) {
        if (e.isJsonArray() || e.isJsonObject()) {
            this.root = e;
        } else {
            throw new IllegalArgumentException("Provided JsonElement is not an object nor an array.");
        }
    }
    
    public JsonConfig(String json) {
        this(new JsonParser().parse(json));
    }

    public Set<String> getKeys(String key) {
        try {
            JsonElement e = get(key);
            if (e == null || !e.isJsonObject()) {
                return null;
            }
            Set<Map.Entry<String, JsonElement>> eset = e.getAsJsonObject().entrySet();
            Set<String> keys = new HashSet<>(eset.size());
            for (Map.Entry<String, JsonElement> ent : eset) {
                keys.add(ent.getKey());
            }
            return keys;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public JsonConfig getSubConfig(String key) {
        try {
            JsonElement e = get(key);
            if (e != null) {
                return new JsonConfig(e);
            }
            return null;
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
    }

    public char getColorChar(String key, char defValue) {
        char c = getChar(key, defValue);
        if (!Character.isDigit(c) && !(c >= 'a' && c <= 'f') && !(c >= 'A' && c <= 'F')) {
            return defValue;
        }
        return c;
    }

    public char getChar(String key, char defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsCharacter();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public String getString(String key, String defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsString();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public boolean getBool(String key, boolean defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsBoolean();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public byte getByte(String key, byte defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsByte();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public short getShort(String key, short defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsShort();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public int getInt(String key, int defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsInt();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public long getLong(String key, long defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsLong();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public BigInteger getBigInteger(String key, BigInteger defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsBigInteger();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public float getFloat(String key, float defValue) {
        return (float) getDouble(key, defValue);
    }

    public double getDouble(String key, double defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsDouble();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defValue) {
        try {
            JsonElement e = get(key);
            if (e == null || e.isJsonNull()) {
                return defValue;
            }
            return e.getAsBigDecimal();
        } catch (IllegalStateException e) {
            return defValue;
        }
    }

    public Map<String, Object> getAsMap(String key) {
        JsonElement e = get(key);
        if (!(e instanceof JsonObject)) {
            return null;
        }
        return new MapWrapper(e.getAsJsonObject());
    }

    public List<Object> getAsList(String key) {
        JsonElement e = get(key);
        if (!(e instanceof JsonArray)) {
            return null;
        }
        return new ListWrapper(e.getAsJsonArray());
    }

    public JsonElement get(String key) {
        JsonAccessor accessor = getAccessor(key);
        return accessor.get(root);
    }

    public void set(String key, Object value) {
        JsonAccessor accessor = getAccessor(key);
        if (key == "") {
            JsonElement e = gson.toJsonTree(value);
            if (e.isJsonObject() || e.isJsonArray()) {
                root = e;
            }
        } else {
            accessor.set(root, gson.toJsonTree(value));
        }
    }

    private JsonAccessor getAccessor(String key) {
        JsonAccessor accessor = accessorMap.get(key);
        if (accessor != null) {
            return accessor;
        }
        List<String> keys = new ArrayList<>(16);
        int keylen = key.length();
        StringBuilder sb = new StringBuilder();
        int mode = -1;
        for (int ci = 0; ci < keylen; ++ci) {
            char c = key.charAt(ci);
            switch (mode) {
                case -1:
                    if (c == '[') {
                        sb.append(c);
                        mode = 2;
                    } else if (Character.isJavaIdentifierPart(c)) {
                        sb.append(c);
                        mode = 0;
                    } else {
                        throw new IllegalArgumentException("Expected array index or map key at start of key: " + key);
                    }
                    break;
                case 0:
                case 1:
                    if (c == '.') {
                        if (sb.length() == 0) {
                            throw new IllegalArgumentException("Missing identifier in key at index " + ci + ": " + key);
                        }
                        keys.add(sb.toString());
                        sb.setLength(0);
                        mode = 0;
                    } else if (c == '[') {
                        if (sb.length() == 0) {
                            throw new IllegalArgumentException("Missing identifier in key at index " + ci + ": " + key);
                        }
                        keys.add(sb.toString());
                        sb.setLength(0);
                        sb.append(c);
                        mode = 2;
                    } else if (mode == 0 && Character.isJavaIdentifierPart(c)) {
                        sb.append(c);
                    } else {
                        throw new IllegalArgumentException("Unexpected character in key at index " + ci + ": " + key);
                    }
                    break;
                case 2:
                    if (Character.isDigit(c)) {
                        sb.append(c);
                        mode = 3;
                    } else if (c == '"') {
                        sb.append(c);
                        mode = 4;
                    } else {
                        throw new IllegalArgumentException("Expected String or number after [ at index " + ci + ": " + key);
                    }
                    break;
                case 3:
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else if (c == ']') {
                        sb.append(c);
                        mode = 1;
                    } else {
                        throw new IllegalArgumentException("Expected number or closing bracket at index " + ci + ": " + key);
                    }
                    break;
                case 4:
                    if (c == '\\') {
                        mode = 6;
                    } else if (c == '"') {
                        sb.append(c);
                        mode = 5;
                    } else {
                        sb.append(c);
                    }
                    break;
                case 5:
                    if (c == ']') {
                        sb.append(c);
                        mode = 1;
                    } else {
                        throw new IllegalArgumentException("Expected closing bracket at index " + ci + ": " + key);
                    }
                    break;
                case 6:
                    switch (c) {
                        case 'u':
                            if (ci + 5 > key.length()) {
                                throw new IllegalArgumentException("Reached end of JSON key while parsing unicode escape: " + key);
                            }
                            sb.append((char) Integer.parseInt(key.substring(ci + 1, ci + 5), 16));
                            ci += 4;
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case '\\':
                        case '"':
                            sb.append(c);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported escape at index " + (ci - 1) + ": " + key);
                    }
                    break;
            }
        }
        if ((mode != 0 && mode != 1) || sb.length() == 0) {
            throw new IllegalArgumentException("Reached end of JSON key while parsing: " + key);
        }
        keys.add(sb.toString());
        List<BiFunction<JsonElement, Object, JsonElement>> accessList = new ArrayList<>(16);
        List<Object> args = new ArrayList<>(16);
        for (String s : keys) {
            if (s.matches("\\[\\d+\\]")) {
                accessList.add(ARRAY_FUNC);
                args.add(Integer.parseInt(s.substring(1, s.length() - 1)));
            } else if (s.matches("\\[\\\".*\\\"\\]")) {
                args.add(s.substring(2, s.length() - 2));
                accessList.add(MAP_FUNC);
            } else {
                args.add(s);
                accessList.add(MAP_FUNC);
            }
        }
        List<BiFunction<JsonElement, Object, JsonElement>> existingList = accessFunctionMap.get(accessList.toString());
        if (existingList == null) {
            accessFunctionMap.put(accessList.toString(), accessList);
        } else {
            accessList = existingList;
        }
        accessor = new JsonAccessor(accessList, args);
        accessorMap.put(key, accessor);
        return accessor;
    }

    public JsonElement getRootElement() {
        return root;
    }

    @Override
    public String toString() {
        return root.toString();
    }
    
    public boolean save(File file) throws IOException {
        boolean noProbs = true;
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(root));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return noProbs;
    }

    public boolean load(File file) throws FileNotFoundException, JsonParseException, IOException {
        if (!file.exists()) {
            file.createNewFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("{}");
                fw.close();
            } catch (Exception e) {
            }
        }
        JsonParser parser = new JsonParser();
        boolean noProbs = true;
        try (FileReader reader = new FileReader(file)) {
            root = parser.parse(reader).getAsJsonObject();
        } catch (FileNotFoundException | JsonParseException e) {
            throw e;
        }
        return noProbs;
    }

    public static final class MapWrapper implements Map<String, Object> {

        private JsonObject wrapped;

        public MapWrapper(JsonObject wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int size() {
            return wrapped.size();
        }

        @Override
        public boolean isEmpty() {
            return wrapped.size() == 0;
        }

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof String)) {
                return false;
            }
            return wrapped.has((String) key);
        }

        @Override
        public boolean containsValue(Object value) {
            JsonElement je = gson.toJsonTree(value);
            for (Map.Entry<String, JsonElement> ent : wrapped.entrySet()) {
                if (je.equals(ent.getValue())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object get(Object key) {
            if (!(key instanceof String)) {
                return null;
            }
            return unJson(wrapped.get((String) key));
        }

        @Override
        public Object put(String key, Object value) {
            JsonElement je = wrapped.get(key);
            wrapped.add(key, gson.toJsonTree(value));
            return unJson(je);
        }

        @Override
        public Object remove(Object key) {
            if (!(key instanceof String)) {
                return null;
            }
            return unJson(wrapped.remove((String) key));
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            for (Map.Entry<? extends String, ? extends Object> ent : m.entrySet()) {
                put(ent.getKey(), ent.getValue());
            }
        }

        @Override
        public void clear() {
            Set<String> props = new HashSet<>();
            for (Map.Entry<String, JsonElement> ent : wrapped.entrySet()) {
                props.add(ent.getKey());
            }
            for (String s : props) {
                remove(s);
            }
        }

        @Override
        public Set<String> keySet() {
            Set<String> props = new HashSet<>();
            for (Map.Entry<String, JsonElement> ent : wrapped.entrySet()) {
                props.add(ent.getKey());
            }
            return props;
        }

        @Override
        public Collection<Object> values() {
            List<Object> props = new ArrayList<>(wrapped.size());
            for (Map.Entry<String, JsonElement> ent : wrapped.entrySet()) {
                props.add(ent.getValue());
            }
            return props;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            Set<Map.Entry<String, Object>> props = new HashSet<>();
            for (final Map.Entry<String, JsonElement> ent : wrapped.entrySet()) {
                props.add(new Entry<String, Object>() {

                    @Override
                    public Object setValue(Object value) {
                        Object o = unJson(ent.getValue());
                        ent.setValue(gson.toJsonTree(value));
                        return o;
                    }

                    @Override
                    public Object getValue() {
                        return unJson(ent.getValue());
                    }

                    @Override
                    public String getKey() {
                        return ent.getKey();
                    }
                });
            }
            return props;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("MapWrapper{");
            boolean comma = false;
            for (Map.Entry<String, Object> ent : entrySet()) {
                if (comma) {
                    sb.append(", ");
                }
                comma = true;
                sb.append(ent.getKey());
                sb.append('=');
                sb.append(ent.getValue().toString());
            }
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ListWrapper implements List<Object> {

        private final JsonArray wrapped;

        private ListWrapper(JsonArray wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int size() {
            return wrapped.size();
        }

        @Override
        public boolean isEmpty() {
            return wrapped.size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            JsonElement e = gson.toJsonTree(o);
            for (int i = wrapped.size() - 1; i >= 0; --i) {
                if (wrapped.get(i).equals(e)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Iterator<Object> iterator() {
            return new Iterator<Object>() {
                private final Iterator<JsonElement> wrappedIterator = wrapped.iterator();

                @Override
                public boolean hasNext() {
                    return wrappedIterator.hasNext();
                }

                @Override
                public Object next() {
                    JsonElement n = wrappedIterator.next();
                    return unJson(n);
                }
            };
        }

        @Override
        public Object[] toArray() {
            Object[] a = new Object[wrapped.size()];
            for (int i = a.length - 1; i >= 0; --i) {
                a[i] = unJson(wrapped.get(i));
            }
            return a;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            int size = wrapped.size();
            if (a.length < size) {
                return (T[]) Arrays.copyOf(toArray(), size, a.getClass());
            }
            if (a.length > size) {
                a[size] = null;
            }
            return a;
        }

        @Override
        public boolean add(Object e) {
            wrapped.add(gson.toJsonTree(e));
            return true;
        }

        @Override
        public boolean remove(Object o) {
            JsonElement je = gson.toJsonTree(o);
            int size = wrapped.size();
            for (int i = 0; i < size; ++i) {
                if (je.equals(wrapped.get(i))) {
                    wrapped.remove(i);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends Object> c) {
            for (Object o : c) {
                add(o);
            }
            return !c.isEmpty();
        }

        @Override
        public boolean addAll(int index, Collection<? extends Object> c) {
            for (Object o : c) {
                add(index++, o);
            }
            return !c.isEmpty();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean change = false;
            for (Object o : c) {
                change |= remove(o);
            }
            return change;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean change = false;
            for (int i = wrapped.size() - 1; i >= 0; --i) {
                if (!c.contains(unJson(wrapped.get(i)))) {
                    remove(i);
                    change = true;
                }
            }
            return change;
        }

        @Override
        public void clear() {
            for (int i = wrapped.size() - 1; i >= 0; --i) {
                wrapped.remove(i);
            }
        }

        @Override
        public Object get(int index) {
            return unJson(wrapped.get(index));
        }

        @Override
        public Object set(int index, Object element) {
            return unJson(wrapped.set(index, gson.toJsonTree(element)));
        }

        @Override
        public void add(int index, Object element) {
            wrapped.add(JsonNull.INSTANCE);
            for (int i = wrapped.size() - 1; i > index; --i) {
                wrapped.set(i, wrapped.get(i - 1));
            }
            wrapped.set(index, gson.toJsonTree(element));
        }

        @Override
        public Object remove(int index) {
            return unJson(wrapped.remove(index));
        }

        @Override
        public int indexOf(Object o) {
            JsonElement e = gson.toJsonTree(o);
            int size = wrapped.size();
            for (int i = 0; i < size; ++i) {
                if (e.equals(wrapped.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            JsonElement e = gson.toJsonTree(o);
            for (int i = wrapped.size() - 1; i >= 0; --i) {
                if (e.equals(wrapped.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public ListIterator<Object> listIterator() {
            return listIterator(-1);
        }

        @Override
        public ListIterator<Object> listIterator(final int index) {
            ListIterator<Object> lit = new ListIterator<Object>() {

                int i = index;

                @Override
                public void set(Object e) {
                    ListWrapper.this.set(i, e);
                }

                @Override
                public void remove() {
                    ListWrapper.this.remove(i);
                }

                @Override
                public int previousIndex() {
                    return i - 1;
                }

                @Override
                public Object previous() {
                    return ListWrapper.this.get(--i);
                }

                @Override
                public int nextIndex() {
                    return i + 1;
                }

                @Override
                public Object next() {
                    return ListWrapper.this.get(++i);
                }

                @Override
                public boolean hasPrevious() {
                    return i > 0;
                }

                @Override
                public boolean hasNext() {
                    return i < ListWrapper.this.size() - 1;
                }

                @Override
                public void add(Object e) {
                    ListWrapper.this.add(i, e);
                }
            };
            return lit;
        }

        @Override
        public List<Object> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException("Cannot return sub-view of wrapped JsonArray.");
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("ListWrapper[");
            boolean comma = false;
            for (Object o : this) {
                if (comma) {
                    sb.append(", ");
                }
                comma = true;
                sb.append(o.toString());
            }
            sb.append(']');
            return sb.toString();
        }
    }

    private static class JsonAccessor {

        private final List<BiFunction<JsonElement, Object, JsonElement>> accessList;
        private final List<Object> args;

        public JsonAccessor(List<BiFunction<JsonElement, Object, JsonElement>> accessList, List<Object> args) {
            this.accessList = new ArrayList(accessList);
            this.args = new ArrayList(args);
        }

        public JsonElement get(JsonElement root) {
            for (int i = 0; i < args.size() && root != null; ++i) {
                root = accessList.get(i).apply(root, args.get(i));
            }
            return root;
        }

        public void set(JsonElement root, JsonElement value) {
            JsonElement last = null;
            Object lastArg = null;
            BiFunction<JsonElement, Object, JsonElement> lastAccessor = null;
            for (int i = 0; i < args.size(); ++i) {
                BiFunction<JsonElement, Object, JsonElement> accessor = accessList.get(i);

                // Ensure type of root, set last's value if needed.
                if (accessor == MAP_FUNC && !(root instanceof JsonObject)) {
                    if (last == null) {
                        return;
                    } else if (last instanceof JsonArray) {
                        JsonArray lastArr = last.getAsJsonArray();
                        while (lastArr.size() < (Integer) lastArg + 1) {
                            lastArr.add(JsonNull.INSTANCE);
                        }
                        lastArr.set((Integer) lastArg, root = new JsonObject());
                    } else if (last instanceof JsonObject) {
                        last.getAsJsonObject().add((String) lastArg, root = new JsonObject());
                    }
                } else if (accessor == ARRAY_FUNC && !(root instanceof JsonArray)) {
                    if (last == null) {
                        return;
                    } else if (last instanceof JsonArray) {
                        JsonArray lastArr = last.getAsJsonArray();
                        while (lastArr.size() < (Integer) lastArg + 1) {
                            lastArr.add(JsonNull.INSTANCE);
                        }
                        lastArr.set((Integer) lastArg, root = new JsonArray());
                    } else if (last instanceof JsonObject) {
                        last.getAsJsonObject().add((String) lastArg, root = new JsonArray());
                    }
                }

                // set last to current
                lastArg = args.get(i);
                last = root;
                lastAccessor = accessor;

                // ensure json array size
                if (root instanceof JsonArray) {
                    JsonArray ja = root.getAsJsonArray();
                    while (ja.size() < (Integer) lastArg + 1) {
                        ja.add(JsonNull.INSTANCE);
                    }
                }

                root = accessor.apply(root, args.get(i));
            }
            if (lastAccessor == ARRAY_FUNC) {
                last.getAsJsonArray().set((Integer) lastArg, value);
            } else if (lastAccessor == MAP_FUNC) {
                last.getAsJsonObject().add((String) lastArg, value);
            }
        }

        @Override
        public String toString() {
            return "JsonAccessor{" + accessList + "," + args + "}";
        }

        @Override
        public int hashCode() {
            return accessList.toString().hashCode() ^ args.toString().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof JsonAccessor)) {
                return false;
            }
            JsonAccessor ja = (JsonAccessor) o;
            return accessList.equals(ja.accessList) && args.equals(ja.args);
        }
    }
}
