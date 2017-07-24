package de.bitsharesmunich.cryptocoincore.insightapi;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Generatir fir tge okhttp connection of the Insight API
 * TODO finish documentation
 */

class InsightApiServiceGenerator {
    /**
     * Tag used for logging
     */
    public static String TAG = "InsightApiServiceGenerator";
    /**
     * The complete uri to connect to the insight api, this change from coin to coin
     */
    private static String sApiBaseUrl;
    /**
     * Loggin interceptor
     */
    private static HttpLoggingInterceptor sLogging;
    /**
     * Http builder
     */
    private static OkHttpClient.Builder sClientBuilder;
    /**
     * Builder for the retrofit class
     */
    private static Retrofit.Builder sBuilder;
    /**
     *
     */
    private static HashMap<Class<?>, Object> sServices;

    /**
     *  Constructor, using the url of a insigth api coin
     * @param apiBaseUrl The complete url to the server of the insight api
     */
    InsightApiServiceGenerator(String apiBaseUrl) {
        sApiBaseUrl= apiBaseUrl;
        sLogging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        sClientBuilder = new OkHttpClient.Builder().addInterceptor(sLogging);
        sBuilder = new Retrofit.Builder().baseUrl(sApiBaseUrl).addConverterFactory(GsonConverterFactory.create());
        sServices = new HashMap<>();
    }

    /**
     *
     * @param klass
     * @param thing
     * @param <T>
     */
    private static <T> void setService(Class<T> klass, T thing) {
        sServices.put(klass, thing);
    }

    /**
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public <T> T getService(Class<T> serviceClass) {

        T service = serviceClass.cast(sServices.get(serviceClass));
        if (service == null) {
            service = createService(serviceClass);
            setService(serviceClass, service);
        }
        return service;
    }

    /**
     *
     * @param serviceClass
     * @param <S>
     * @return
     */
    private static <S> S createService(Class<S> serviceClass) {

        sClientBuilder.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request original = chain.request();
                okhttp3.Request.Builder requestBuilder = original.newBuilder().method(original.method(), original.body());

                okhttp3.Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        sClientBuilder.readTimeout(5, TimeUnit.MINUTES);
        sClientBuilder.connectTimeout(5, TimeUnit.MINUTES);
        OkHttpClient client = sClientBuilder.build();
        Retrofit retrofit = sBuilder.client(client).build();
        return retrofit.create(serviceClass);

    }

    /**
     *
     * @return
     */
    public static InsightApiService Create() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request original = chain.request();

                // Customize the request
                okhttp3.Request request = original.newBuilder().method(original.method(), original.body()).build();

                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(sApiBaseUrl).client(client).build();
        return retrofit.create(InsightApiService.class);
    }
}
