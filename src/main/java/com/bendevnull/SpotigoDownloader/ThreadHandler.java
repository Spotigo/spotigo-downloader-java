package com.bendevnull.SpotigoDownloader;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadHandler extends Thread {

    public List<Thread> threads = new CopyOnWriteArrayList<Thread>();
    public List<Thread> running = new CopyOnWriteArrayList<Thread>();

    public void register(Thread t) {
        this.threads.add(t);
    }

    public void run() {
        while (threads.size() > 0 || running.size() > 0) {
            while (running.size() < 2 && threads.size() > 0) {
                if (threads.size() > 0) {
                    Thread t = threads.get(0);
                    t.start();
                    threads.remove(0);
                    running.add(t);
                }
            }
            for (Thread t : running) {
                if (!t.isAlive()) {
                    running.remove(t);
                }
            }
        }
    }
}