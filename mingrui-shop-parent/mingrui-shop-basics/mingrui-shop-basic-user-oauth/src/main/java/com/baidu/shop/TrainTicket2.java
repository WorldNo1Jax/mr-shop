package com.baidu.shop;

/**
 * @ClassName TrainTicket2
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class TrainTicket2 implements Runnable {
    private int ticketCount = 100; // 初始化100张火车票(全局变量)
    static Object mutex = new Object(); // 多个线程使用同一把锁mutex
    public boolean flag = true;

    @Override
    public void run() {
        if (flag) {
//			synchronized (this) { // synchronized放这里只有一个线程在执行,why?
            while (ticketCount > 0) {
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                }
                synchronized (this) { // synchronized放这里出现第101张票，发生线程不安全问题,why?
                    System.out.println(
                            Thread.currentThread().getName() + "<true>抢到了第【" + (100 - ticketCount + 1) + "】张火车票");
                    ticketCount--;
                }
            }
        } else {
            shopTicket();
        }
    }

    private synchronized void shopTicket() {
        while (ticketCount > 0) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
            }
            System.out.println(Thread.currentThread().getName() + ":<false>抢到了第【" + (100 - ticketCount + 1) + "】张火车票");
            ticketCount--;
        }
    }

}