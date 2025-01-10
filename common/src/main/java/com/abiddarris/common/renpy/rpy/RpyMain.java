package com.abiddarris.common.renpy.rpy;

import static com.abiddarris.common.files.Files.changeExtension;
import static com.abiddarris.common.files.Files.getExtension;
import static com.abiddarris.common.files.Files.getNameWithoutExtension;
import static com.abiddarris.common.renpy.internal.Builtins.None;
import static com.abiddarris.common.stream.InputStreams.readAll;

import com.abiddarris.common.renpy.internal.Sys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RpyMain {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            new RpyMain().realMain(args);
        } finally {
            System.out.println(System.currentTimeMillis() - startTime);
        }
    }

    private List<File> filesToProcess = new ArrayList<>();

    private void realMain(String[] args) throws IOException, InterruptedException {
        Rpy.initLoader();
        addDirectoryToProcessed(new File("/home/abid/Desktop/renpy2/"));

        for (File file : filesToProcess) {
            String extension = getDecompiledExtension(file);

            Process process = new ProcessBuilder(new String[]{
                    "/home/abid/Programming/unrpyc/unrpyc.py",
                    "-c",
                    file.getAbsolutePath()
            }).start();
            process.waitFor();

            System.out.println(new String(readAll(process.getInputStream())));
            System.err.println(new String(readAll(process.getErrorStream())));

            File decompiledFile = changeExtension(file, extension);
            File originalDecompiledFile = new File(file.getParent(), getNameWithoutExtension(file) + "_original." + extension);

            RpyDecompiler.decompile(new File("/home/abid/Programming/just yuri/splash.rpyc"), false, true, None);

            if (originalDecompiledFile.exists()) {
                originalDecompiledFile.delete();
            }

            if (!decompiledFile.renameTo(originalDecompiledFile)) {
                throw new IOException("Cannot rename " + decompiledFile + " to " + originalDecompiledFile);
            }

            try {
                Unrpyc.decompile_rpyc(file, new Unrpyc.Context(), false,
                        false, false, false, false, None, true, None);
            } catch (Exception e) {
                e = new IOException("Cannot decompiled " + file.getName(), e);
                e.printStackTrace();
            }

            if (!decompiledFile.exists()) {
                System.err.println("Error : " + decompiledFile.getName() + " does not exist");
                continue;
            }

            process = new ProcessBuilder(new String[]{
                    "diff",
                    "--color=always",
                    originalDecompiledFile.getAbsolutePath(),
                    decompiledFile.getAbsolutePath()
            }).start();

            System.out.println(new String(readAll(process.getInputStream())));
            System.err.println(new String(readAll(process.getErrorStream())));
        }
    }

    private String getDecompiledExtension(File file) {
        String extension = getExtension(file);
        if (extension.equals("rpyc")) {
            return "rpy";
        }

        if (extension.equals("rpymc")) {
            return "rpym";
        }

        throw new IllegalArgumentException("Unknown extension : " + extension);
    }

    private void addDirectoryToProcessed(File file) {
        for (File f : file.listFiles()) {
            String extension = getExtension(f);
            if (extension.equals("rpyc") || extension.equals("rpymc")) {
                filesToProcess.add(f);
            }
        }
    }

}
