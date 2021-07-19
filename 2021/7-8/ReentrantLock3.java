package tech.ibook.saas.test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLock3 {
    static Lock lock1=new ReentrantLock();
    static Lock lock2=new ReentrantLock();

    public static void main(String[] args)  {
        Thread thread=new Thread(new ThreadDemo(lock1,lock2));
        Thread thread1=new Thread(new ThreadDemo(lock2,lock1));
        thread.start();
        thread1.start();
        thread.interrupt();
    }
}
