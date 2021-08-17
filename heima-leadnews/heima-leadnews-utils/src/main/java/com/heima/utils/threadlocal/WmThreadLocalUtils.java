package com.heima.utils.threadlocal;


import com.heima.model.media.pojos.WmUser;

public class WmThreadLocalUtils {

    private final static ThreadLocal<WmUser> userThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程中的用户
     * @param wmUser
     */
    public static void setUser(WmUser wmUser){
        userThreadLocal.set(wmUser);
    }

    /**
     * 获取当前线程中的用户
     * @return
     */
    public static WmUser getUser(){
        return userThreadLocal.get();
    }

}
