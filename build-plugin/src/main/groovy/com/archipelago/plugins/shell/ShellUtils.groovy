package com.archipelago.plugins.shell;

import org.gradle.api.GradleException
import org.gradle.internal.os.OperatingSystem;


class ShellUtils {
    public static File getExecutableOnPath(String exeName) {
        def pathExt = OperatingSystem.current().isWindows() ? System.getenv('PATHEXT')?.split(';') : ['']
        def paths = System.getenv('PATH')?.split(OperatingSystem.current().isWindows() ? ';' : ':') ?: []
        for (dir in paths) {
            for (ext in pathExt) {
                def file = new File(dir, exeName + ext);
                if (file.exists() && file.canExecute()) {
                    return file
                }
            }
        }
        return null;
    }

    public static String executeCommand(String[] command, File directory) {
        return executeCommand(command, directory, new HashMap<String, String>());
    }


    static void execute(String[] command, File directory, Map<String, String> extraEnv = [:]) {
        println("Executing command: ${command.join(' ')} in ${directory}")

        def pb = new ProcessBuilder(command)
        pb.directory(directory)
        pb.environment().putAll(extraEnv ?: [:])
        // Preserve both stdout/stderr with full color formatting
//        pb.inheritIO()

        def process = pb.start()
        // Redirect standard output and error explicitly
        Thread.start {
            InputStream input = process.getInputStream()
            int c
            while ((c = input.read()) != -1) {
                System.out.print((char) c)
            }
        }

        Thread.start {
            InputStream err = process.getErrorStream()
            int c
            while ((c = err.read()) != -1) {
                System.err.print((char) c)
            }
        }
        def exitCode = process.waitFor()

        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code ${exitCode}: ${command.join(' ')}")
        }
    }

    public static String executeCommand(String[] command, File directory, Map<String, String> env) {
        return executeCommand(command, directory, env, null);
    }

    public static String executeCommand(String[] command, File directory, Map<String, String> env, String stdIn) {
        try {
            // Command execution is intentionally silent; enable for debugging if needed
            // println "Executing " + Arrays.asList(command);
            Map<String, String> combinedEnv = new HashMap<String, String>(System.getenv());
            if (env != null)
                combinedEnv.putAll(env);
            def process = new ProcessBuilder(command)
                    .directory(directory)
//                    .environment(combinedEnv)
                    .redirectErrorStream(true)
                    .start()
            if (stdIn != null) {
                process.outputStream.write(stdIn.bytes)
                process.outputStream.flush()
                process.outputStream.close()
            }
            def output = process.inputStream.text.trim()
            process.waitFor()

            if (process.exitValue() != 0) {
                throw new GradleException("Failed command: ${command} \n${output} " + output)
            }

            return output
        } catch (Exception e) {
            throw new GradleException("Error executing Git command: " + e.message, e)
        }
    }

}