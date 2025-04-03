///Цель: вузять весь HTML код с сайта mail.ru и вывести его в логе
package com.example.downloadwebcontent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ///Эта переменная может использоваться для хранения URL-адреса сайта Mail.ru, чтобы потом использовать его в коде.
    private String mailRu = "https://mail.ru/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///Создаём объект для запуска 2 потока
        DownloadTask task = new DownloadTask();
        /*метод чтобы запустить задачу (запуститься в другом потоке)
        чтобы получить данные которые вернёт этот метод (execute) --- .get()*/ //всё это оборачиваем в переменную
        ///Ctrl + alt + t --- быстрый оборот (в try-catch)
        try {
            String result = task.execute(mailRu).get();
            ///New changes
            if (result != null) {
                Log.i("URL", result);
            } else {
                Log.e("URL", "Failed to download content");
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    ///UI Thread - поток пользовательского интерфейса (запуск другого потока)
    /*static --- доступ к нему только из этого класса; extends --- данный класс будет расширять другой класс, AsyncTask нужен для того чтобы задача выполнялась в другом потоке
    1. String --- те данные которые мы будем отправлять в DownloadTask(он относится ссылке mail.ru) 2. Void --- данные которые будут передаваться в процессе загрузки(кружок загрузки)  3. String --- данные которые будут возвращаться после выполнения задачи DownloadTask(1 string, передаст весь код mail.ru)*/
    private static class DownloadTask extends AsyncTask<String, Void, String> {     //AsyncTask --- обстрактный,  надо реализовать метод (ctrl + i)

        @Override
        protected String doInBackground(String... strings) {        // ... --- массив строк
            /*Log.i(): Это метод для вывода информации в Logcat (консоль отладки).
            strings[0]: Это первый элемент массива strings. Так как strings — это массив строк, то strings[0] — это первая строка, переданная в doInBackground().
            Что делает эта строка: Эта строка выводит в Logcat информацию о первом URL-адресе, переданном в doInBackground().
            //Log.i("URL", strings[0]);    тк это массив то мы выбираем элемент данного массива который мы передали (один единствен mailRu)*/
            ///Для загрузке контента из интренета, надо ...
            StringBuilder result = new StringBuilder();     //в неё формируется контент (копирование)
            URL url = null;
            HttpURLConnection urlConnection = null;         //принцип работы как у браузера (открывает url и берёт данные)
            BufferedReader bufferedReader = null;
            try {
                /*в скобках передаём строку с адресом
                тк. 1 параметр поэтому strings[0]*/
                url = new URL(strings[0]);
                /*открытие соединения может быть с ошибкой поэтому обрабатываем в блоке catch
                 * затем выйдет ещё ошибка тк несовместимые типы (нужен HTTPconection, а openConnection возвращает URLconnection)
                 * ПРЕОБРАЗУЕМ URLconnection в HttpURLConnection*/
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();        //получаем поток ввода, для дальнейшего чтения данных из данного соединения
                InputStreamReader reader = new InputStreamReader(in);     //для чтения данных из интернета ^^^^^
                bufferedReader = new BufferedReader(reader);     //читаем данные из интернета строками :)
                String line = bufferedReader.readLine();        //создаём строку которую будем читать (получили 1 строчку из сайта) читаем только 1 строку из reader

                ///Делаем процесс чтения пока все данные по строкам не будут прочитанны
                while (line != null) {
                    result.append(line);        //прочитав строку доб. в StringBuilder (она в своё время относится к result, который мы вернём return)
                    line = bufferedReader.readLine();       //переменной присваиваем значение следущей строки
                }

            } catch (MalformedURLException e) {
                Log.e("Error", "MalformedURLException: " + e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error", "IOException: " + e.getMessage());
                return null;
                ///Finaly --- если мы открыли соединение, но при чтении данных произошла ошибка то сработает искл. НО соединение так и останется открытым И его нужно закрыть!!!
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                ///New changes
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e("Error", "Error closing BufferedReader: " + e.getMessage());
                    }
                }
            }

            return result.toString();///
        }
    }
}