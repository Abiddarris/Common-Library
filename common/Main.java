
import java.io.File;

import com.abiddarris.common.renpy.internal.PythonObject;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Builtins.None;
import static com.abiddarris.common.renpy.internal.Builtins.__import__;
import static com.abiddarris.common.renpy.internal.Builtins.object;
import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newFunction;
import static com.abiddarris.common.renpy.internal.core.functions.Functions.newFunction;

import com.abiddarris.common.renpy.rpy.Rpy;
import com.abiddarris.common.renpy.rpy.Unrpyc;
import com.abiddarris.common.renpy.rpy.RpyMain;

public class Main {
    public static void main(String[] args) throws Exception {
        RpyMain.main(args);
    }

    static int a = 0;

    private static PythonObject func(PythonObject ac, PythonObject b) {
        a += 1;

        return None;
    }

    public static void a() {
        PythonObject func;
        func = newFunction(Main.class, "func", "a", "b");

        PythonObject a = newInt(1);

        for (int i = 0; i < 900000; i++) {
            func.call(a, a);
        }

        //actual test
        Main.a = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            func.call(a, a);
        }

        System.out.println(System.currentTimeMillis() - startTime + " ms");

        func = newFunction(Main::func, "a", "b");
        // warm up
        Main.a = 0;

        for (int i = 0; i < 900000; i++) {
            func.call(a, a);
        }

        //actual test
        Main.a = 0;
        startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            func.call(a, a);
        }

        System.out.println(System.currentTimeMillis() - startTime + " ms");


//        PythonObject obj = object.call();
//        PythonObject n = newInt(3);
//
//        for (int i = 0; i < 100000; i++) {
//            obj.setAttribute("s", n);
//        }
    }

}