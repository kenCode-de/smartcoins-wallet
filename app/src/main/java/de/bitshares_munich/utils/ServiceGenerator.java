package de.bitshares_munich.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by root on 1/21/16.
 */
public class ServiceGenerator {
    public static String TAG = "ServiceGenerator";
    public static String API_BASE_URL;
    private static HttpLoggingInterceptor logging;
    private static OkHttpClient.Builder clientBuilder;
    private static Retrofit.Builder builder;

    private static HashMap<Class<?>, Object> Services;

    public ServiceGenerator(String apiBaseUrl) {
        API_BASE_URL = apiBaseUrl;
        logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder = new OkHttpClient.Builder().addInterceptor(logging);
        builder = new Retrofit.Builder().baseUrl(API_BASE_URL).addConverterFactory(GsonConverterFactory.create());
        Services = new HashMap<Class<?>, Object>();
    }

    public static <T> void setService(Class<T> klass, T thing) {
        Services.put(klass, thing);
    }

    public <T> T getService(Class<T> serviceClass) {

        T service = serviceClass.cast(Services.get(serviceClass));
        if (service == null) {
            service = createService(serviceClass);
            setService(serviceClass, service);
        }
        return service;
    }

    public static <S> S createService(Class<S> serviceClass) {

        clientBuilder.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request original = chain.request();
                okhttp3.Request.Builder requestBuilder = original.newBuilder().method(original.method(), original.body());

                okhttp3.Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        clientBuilder.readTimeout(5, TimeUnit.MINUTES);
        clientBuilder.connectTimeout(5, TimeUnit.MINUTES);
        OkHttpClient client = clientBuilder.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);

    }

    public static IWebService Create() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request original = chain.request();

                // Customize the request
                okhttp3.Request request = original.newBuilder().method(original.method(), original.body()).build();

                okhttp3.Response response = chain.proceed(request);

                return response;
            }
        });

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_BASE_URL).client(client).build();

        return retrofit.create(IWebService.class);

    }
}