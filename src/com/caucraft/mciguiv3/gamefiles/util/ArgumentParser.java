package com.caucraft.mciguiv3.gamefiles.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author caucow
 */
public class ArgumentParser {
    
    private static final Pattern varPattern;
    private List<Argument> jvmArgs;
    private String mainClass;
    private List<Argument> gameArgs;
    
    static {
        varPattern = Pattern.compile("\\$\\{.*?\\}");
    }
    
    public ArgumentParser(String mainClass) {
        this.jvmArgs = new ArrayList<>();
        this.mainClass = mainClass;
        this.gameArgs = new ArrayList<>();
    }
    
    public ArgumentParser(String mainClass, ArgumentParser copy) {
        this.jvmArgs = new ArrayList(copy.jvmArgs);
        this.mainClass = mainClass;
        this.gameArgs = new ArrayList(copy.gameArgs);
    }
    
    public void addJvmArg(Argument arg) {
        jvmArgs.add(arg);
    }
    
    public void addGameArg(Argument arg) {
        gameArgs.add(arg);
    }
    
    public List<String> compile(String command, List<String> extraJvmArgs, Map<String, String> properties, List<String> extraGameArgs) {
        List<String> fin = new ArrayList<>(
                (extraJvmArgs == null ? 0 : extraJvmArgs.size())
                + jvmArgs.size()
                + gameArgs.size()
                + (extraGameArgs == null ? 0 : extraGameArgs.size())
                + 2);
        fin.add(command);
        if (extraJvmArgs != null) {
            for (String s : extraJvmArgs) {
                fin.add(process(s, properties));
            }
        }
        for (Argument a : jvmArgs) {
            if (a.passRules(properties)) {
                fin.add(process(a.arg, properties));
            }
        }
        fin.add(mainClass);
        for (Argument a : gameArgs) {
            if (a.passRules(properties)) {
                fin.add(process(a.arg, properties));
            }
        }
        if (extraGameArgs != null) {
            for (String s : extraGameArgs) {
                fin.add(process(s, properties));
            }
        }
        return fin;
    }
    
    private String process(String arg, Map<String, String> properties) {
        int last = 0;
        StringBuilder sb = new StringBuilder();
        Matcher m = varPattern.matcher(arg);
        while (m.find()) {
            sb.append(arg.substring(last, m.start()));
            String g = m.group();
            String repl = properties.get(g.substring(2, g.length() - 1));
            sb.append(repl);
            last = m.end();
        }
        sb.append(arg.substring(last));
        return sb.toString();
    }
}
