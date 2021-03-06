package com.jess.wideeyes.app;

import android.content.Context;

import com.jess.arms.base.BaseApplication;
import com.jess.arms.http.GlobeHttpResultHandler;
import com.jess.arms.utils.UiUtils;
import com.jess.wideeyes.di.component.AppComponent;
import com.jess.wideeyes.di.component.DaggerAppComponent;
import com.jess.wideeyes.di.module.CacheModule;
import com.jess.wideeyes.di.module.ServiceModule;
import com.jess.wideeyes.mvp.model.api.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.jessyan.rxerrorhandler.handler.listener.ResponseErroListener;
import okhttp3.Interceptor;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by jess on 8/5/16 11:07
 * contact with jess.yan.effort@gmail.com
 */
public class WEApplication extends BaseApplication {
    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppComponent = DaggerAppComponent
                .builder()
                .appModule(getAppModule())//baseApplication提供
                .clientModule(getClientModule())//baseApplication提供
                .imageModule(getImageModule())//baseApplication提供
                .serviceModule(new ServiceModule())//需自行创建
                .cacheModule(new CacheModule())//需自行创建
                .build();
    }

    @Override
    public String getBaseUrl() {
        return Api.APP_DOMAIN;
    }

    /**
     * 将AppComponent返回出去,供其它地方使用, AppComponent接口中声明的方法返回的实例, 在getAppComponent()拿到对象后都可以直接使用
     * @return
     */
    public AppComponent getAppComponent() {
        return mAppComponent;
    }


    /**
     * 这里可以提供一个全局处理http响应结果的处理类,
     * 这里可以比客户端提前一步拿到服务器返回的结果,可以做一些操作,比如token超时,重新获取
     * 默认不实现,如果有需求可以重写此方法
     *
     * @return
     */
    @Override
    public GlobeHttpResultHandler getHttpResultHandler() {
        return new GlobeHttpResultHandler() {
            @Override
            public Response onHttpResultResponse(String httpResult, Interceptor.Chain chain, Response response) {
                //这里可以先客户端一步拿到每一次http请求的结果,可以解析成json,做一些操作,如检测到token过期后
                //重新请求token,并重新执行请求
                try {
                    JSONArray array = new JSONArray(httpResult);
                    JSONObject object = (JSONObject) array.get(0);
                    String login = object.getString("login");
                    String avatar_url = object.getString("avatar_url");
                    Timber.tag(TAG).w("result ------>" + login + "    ||   avatar_url------>" + avatar_url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //这里如果发现token过期,可以先请求最新的token,然后在拿新的token去重新请求之前的http请求
                // create a new request and modify it accordingly using the new token
//                Request newRequest = chain.request().newBuilder().header("token", newToken)
//                        .build();
//
//                // retry the request
//
//                originalResponse.body().close();
//                return chain.proceed(newRequest);
                //如果需要返回新的结果,则直接把response参数返回出去
                return response;
            }
        };
    }

    @Override
    protected ResponseErroListener getResponseErroListener() {
        return new ResponseErroListener() {
            @Override
            public void handleResponseError(Context context, Exception e) {
                Timber.tag(TAG).w("------------>" + e.getMessage());
                UiUtils.SnackbarText("net error");
            }
        };
    }
}
