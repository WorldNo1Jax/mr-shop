package com.baidu.shop;

/**
 * @ClassName MutilThreadDemo_02
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class MutilThreadDemo_02 {
    public static void main(String[] args) throws InterruptedException {
        /* 1、两个线程、两个实体，无法验证 */
        // TrainTicket2 tt1 = new TrainTicket2();
        // TrainTicket2 tt2 = new TrainTicket2();
        // new Thread(tt1, "窗口①").start();
        // new Thread(tt2, "窗口二").start();
        /* 未发生线程安全问题 */

        /* 2、两个线程、同一个实体、调用相同的方法，无法验证 */
//		 TrainTicket2 tt = new TrainTicket2();
//		 new Thread(tt, "窗口①").start();
//		 new Thread(tt, "窗口二").start();
        /* 出现线程安全问题 :窗口二<true>抢到了第【101】张火车票.why? */

        /* 3、两个线程、同一个实体、调用不同的方法，验证synchronized使用this锁 */
        TrainTicket2 tt = new TrainTicket2();
        new Thread(tt, "窗口①").start();
        Thread.sleep(40);
        tt.flag = false;
        new Thread(tt, "窗口二").start();
        /* 出现线程安全问题 :相同的实体，同一把锁，为什么出现了第【101】张火车票? */
    }

}
