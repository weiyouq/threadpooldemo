package com.kenny.threadpool.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author kenny_peng
 * @created 2019/10/11 下午7:54
 */
public class KennyThreadPool {

    private volatile boolean isWorking = true;

    //1 创建线程仓库
    BlockingQueue<Runnable> blockingQueue;

    //2 创建线程集合
    List<Thread> works;

    //3  具体需要执行的线程方法
    public static class Worker extends Thread {

        private KennyThreadPool pool;

        public Worker(KennyThreadPool pool) {
            this.pool = pool;
        }

        @Override
        public void run() {
            //开始工作
            while (this.pool.isWorking || this.pool.blockingQueue.size() > 0) {
                Runnable task = null;
                try {
                    if (this.pool.isWorking){//使用阻塞的方式获取线程池中的任务
                        task = this.pool.blockingQueue.take();
                    }else {//线程池关闭，使用非阻塞的方式获取已经在线程池中的任务
                        task = this.pool.blockingQueue.poll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (task != null) {
                    task.run();
                    System.out.println("线程" + Thread.currentThread().getName() + "执行完成");
                }
            }
        }

    }

    public KennyThreadPool(int poolSize, int taskSize) {
        if (poolSize <= 0 || taskSize <= 0)
            throw new IllegalArgumentException("非法参数");
        this.blockingQueue = new LinkedBlockingDeque<>(taskSize);
        this.works = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(this);
            worker.start();
            works.add(worker);
        }
    }

    //把任务提交到仓库
    public boolean submit(Runnable task) {
        if (isWorking)
            return this.blockingQueue.offer(task);
        return false;
    }

    public void shutDown(){
        this.isWorking = false;

        for (Thread thread : works){

            if (thread.getState().equals(Thread.State.BLOCKED)){
                thread.interrupt();
            }
        }


    }

    public static void main(String[] args) {
        KennyThreadPool kennyThreadPool = new KennyThreadPool(3, 12);

        for (int i = 0; i < 12; i++) {
//            System.out.println(i);
            kennyThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("放入一个线程");
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        System.out.println("线程被中断");
                    }
                }
            });
        }
        kennyThreadPool.shutDown();
    }


}
