package de.minestar.nightwatch.core;

import java.io.File;

public class Spawner {


    public void startProcess(File jarFile, int minMemoryMB, int maxMemoryMB) throws Exception {
        String minMemory = "-Xms" + minMemoryMB + "M";
        String maxMemory = "-Xmx" + maxMemoryMB + "M";
        ProcessBuilder pBuilder = new ProcessBuilder("java", minMemory, maxMemory, "-jar", jarFile.getAbsolutePath(), "nogui");
        pBuilder.directory(jarFile.getParentFile());
        pBuilder.inheritIO();
        Process start = pBuilder.start();
        System.out.println(start.isAlive());
        Thread.sleep(5000);
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Command:");
//        String com = scanner.nextLine();
//        start.getOutputStream().write(com.getBytes());
//        scanner.close();
        

//        pBuilder.redirectOutput(Redirect.PIPE);
//        pBuilder.redirectOutput(Redirect.PIPE);
        
        System.out.println("Destroy");  
//        start.destroy();        
//        start.waitFor();
        System.out.println("Terminated");

    }

    public static void main(String[] args) {
        Spawner spawner = new Spawner();
        try {
            spawner.startProcess(new File("server/vanilla/minecraft_server.1.7.10.jar"), 1024, 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
