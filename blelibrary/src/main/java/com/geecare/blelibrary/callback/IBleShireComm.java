package com.geecare.blelibrary.callback;

/**
 * Created by Administrator on 2016/7/18.
 */
public interface IBleShireComm
{

    void OnRecvshire_bind(String hex);


    void onRecvshire_unbind(String hex);


    void onRecvshire_set_time(String hex);


    void onRecvshire_get_time(String hex);

    void onRecvshire_get_dosage(String hex);


    void onRecvshire_get_imei(String hex);

    void onRecvshire_get_unknown(String hex);

}
