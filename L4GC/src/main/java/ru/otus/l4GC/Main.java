package ru.otus.l4GC;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class Main {

    //run with parameters:
    //-Xms100m -Xmx100m -XX:MaxMetaspaceSize=100m -verbose:gc
    // -Xloggc:logs/gc_pid_%p.log -XX:+PrintGCDateStamps
    // -XX:+PrintGCDetails -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10
    // -XX:GCLogFileSize=100M -XX:+HeapDumpOnOutOfMemoryError
    // -XX:HeapDumpPath=dumps/ -XX:OnOutOfMemoryError="kill -3 %p"

    static int count = 10000;
    static int delay = 100;
    static String str = "number ";

    public static void main(String[] args) {
        installGCMonitoring();
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void run() throws InterruptedException {
        List<String> list = new ArrayList<>();
        fillList(list, count);

    }

    private static void fillList(List<String> list, int count) throws InterruptedException {
        int size = list.size();
        int from = size/2;
        for (int i = from; i < size; i++) {
            list.set(i,str + String.valueOf(i));
        }
        for (int i = 0; i < count; i++) {
            list.add(str + String.valueOf(i));
        }
        System.out.println("list is full, size = "+list.size());
        Thread.sleep(delay);
        fillList(list, count);
    }

    private static void installGCMonitoring() {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean gcbean: beans){
            NotificationEmitter emitter = (NotificationEmitter) gcbean;

            System.out.println(gcbean.getName());

            NotificationListener listener = (notification, handback)->{
                if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)){
                    GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());

                    long duration = info.getGcInfo().getDuration();
                    String gctype = info.getGcAction();

                    System.out.println(gctype+": - "+info.getGcInfo().getId()+
                            ", "+info.getGcName()+"(from "+info.getGcCause()+") "+duration+" millis");


                }
            };

            emitter.addNotificationListener(listener, null, null);
        }
    }


}
