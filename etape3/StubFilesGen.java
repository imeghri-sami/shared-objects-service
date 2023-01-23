import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class StubFilesGen {

    public static final String GEN_FOLDER = "stubs/";
    private static final String STUB_FIlE_PREFIX = "_stub";
    public static void generate(Object o) {
        String workingDir = System.getProperty("user.dir");

        File genFolder = new File(workingDir + "\\" + GEN_FOLDER);

        if (! genFolder.exists() ) {
            if ( genFolder.mkdir() ) {
                System.out.println("Creating stubs folder ...");
            }else {
                System.err.println("Cannot create stubs folder");
            }
        }

        System.out.println("Generating stub file ...");

        String stubClassName = o.getClass().getSimpleName();
        File stubFile = new File ( workingDir + "\\" + GEN_FOLDER + "\\" +stubClassName + STUB_FIlE_PREFIX + ".java");
        try {
            stubFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileWriter fileWriter = new FileWriter(stubFile);
            fileWriter.write(String.format("public class %s extends SharedObject implements ",
                    stubClassName + STUB_FIlE_PREFIX) +
                    "java.io.Serializable { \n");

            // Get methods
            Method[] methods = o.getClass().getDeclaredMethods();
            for ( Method m : methods) {
                String modifier = Modifier.toString(m.getModifiers()) + " ";
                String returnType = m.getReturnType().getSimpleName() + " ";
                String methodName = m.getName() + " ";
                StringBuilder params = new StringBuilder();
                StringBuilder methodExceptions = new StringBuilder();
                // Parameters
                Class<?>[] parameters = m.getParameterTypes();
                int argCounter = 0;
                for ( Class<?> param : parameters) {
                    params.append(param.getSimpleName()+ " ").append("arg").append(argCounter);
                    if ( argCounter != parameters.length - 1) {
                        params.append(", ");
                    }
                    argCounter++;
                }
                // Exceptions
                Class<?>[] exceptions = m.getExceptionTypes();
                if (exceptions.length != 0) {
                    methodExceptions.append("throws ");
                    int expCounter = 0;

                    for ( Class<?> e : exceptions) {
                        methodExceptions.append(e.getName());
                        if (expCounter != exceptions.length - 1) {
                            methodExceptions.append(", ");
                        }
                        expCounter++;
                    }
                }


                fileWriter.write(modifier + returnType + methodName + "(" + params + ")" + methodExceptions + "\n");
                fileWriter.write("{ ");
                StringBuilder body = new StringBuilder();
                body.append(stubClassName).append(" ")
                        .append("v ")
                        .append(String.format("= (%s) ", stubClassName))
                        .append("obj;\n");
                StringBuilder args = new StringBuilder();
                for( int i = 0; i < argCounter; i++) {
                    args.append("arg").append(i);
                    if (i < argCounter - 1) {
                        args.append(", ");
                    }
                }


                if ( !m.getReturnType().equals(Void.TYPE) ) {
                    body.append("return ");
                }

                body.append("v.").append(m.getName()).append("(").append(args).append(");\n");
                fileWriter.write(body.toString());

                fileWriter.write(" }\n\n");
            }

            fileWriter.write(" }");
            fileWriter.close();
            System.out.println("Generation finished");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
