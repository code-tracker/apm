package com.devcolibri.androidandmysql;

import java.util.ArrayList;
import java.util.List;

import com.devcolibri.parser.JSONParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditProductActivity extends Activity {

    EditText txtName;
    EditText txtPrice;
    EditText txtDesc;
    Button btnSave;
    Button btnDelete;

    String pid;

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();

    // url для получения одного продукта
    private static final String url_product_detials = "http://test.devcolibri.com/get_product_details.php";

    // url для обновления продукта
    private static final String url_update_product = "http://test.devcolibri.com/update_product.php";

    // url для удаления продукта
    private static final String url_delete_product = "http://test.devcolibri.com/delete_product.php";

    // JSON параметры
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "product";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
    private static final String TAG_PRICE = "price";
    private static final String TAG_DESCRIPTION = "description";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_product);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        // показываем форму про детальную информацию о продукте
        Intent i = getIntent();

        // получаем id продукта (pid) с формы
        pid = i.getStringExtra(TAG_PID);

        // Получение полной информации о продукте в фоновом потоке
        new GetProductDetails().execute();

        // обрабочик на кнопку сохранение продукта
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // запускаем выполнение задачи на обновление продукта
                new SaveProductDetails().execute();
            }
        });

        // обработчик на кнопку удаление продукта
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // удалем продукт в фоновом потоке
                new DeleteProduct().execute();
            }
        });

    }

    /**
     * Фоновая асинхронная задача для получения полной информации о продукте
     **/
    class GetProductDetails extends AsyncTask<String, String, String> {

        /**
         * Перед началом показать в фоновом потоке прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditProductActivity.this);
            pDialog.setMessage("Loading product details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Получение детальной информации о продукте в фоновом режиме
         **/
        protected String doInBackground(String[] params) {

            // обновляем UI форму
            runOnUiThread(new Runnable() {
                public void run() {
                    // проверяем статус success тега
                    int success;
                    try {
                        // Список параметров
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("pid", pid));

                        // получаем продукт по HTTP запросу
                        JSONObject json = jsonParser.makeHttpRequest(url_product_detials, "GET", params);

                        Log.d("Single Product Details", json.toString());

                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // Успешно получинна детальная информация о продукте
                            JSONArray productObj = json.getJSONArray(TAG_PRODUCT);

                            // получаем первый обьект с JSON Array
                            JSONObject product = productObj.getJSONObject(0);

                            // продукт с pid найден
                            // Edit Text
                            txtName = (EditText) findViewById(R.id.inputName);
                            txtPrice = (EditText) findViewById(R.id.inputPrice);
                            txtDesc = (EditText) findViewById(R.id.inputDesc);

                            // покаываем данные о продукте в EditText
                            txtName.setText(product.getString(TAG_NAME));
                            txtPrice.setText(product.getString(TAG_PRICE));
                            txtDesc.setText(product.getString(TAG_DESCRIPTION));

                        }else{
                            // продукт с pid не найден
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }

        /**
         * После завершения фоновой задачи закрываем диалог прогресс
         **/
        protected void onPostExecute(String file_url) {
            // закрываем диалог прогресс
            pDialog.dismiss();
        }
    }

    /**
     * В фоновом режиме выполняем асинхроную задачу на сохранение продукта
     **/
    class SaveProductDetails extends AsyncTask<String, String, String> {

        /**
         * Перед началом показываем в фоновом потоке прогрксс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditProductActivity.this);
            pDialog.setMessage("Saving product ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Сохраняем продукт
         **/
        protected String doInBackground(String[] args) {

            // получаем обновленные данные с EditTexts
            String name = txtName.getText().toString();
            String price = txtPrice.getText().toString();
            String description = txtDesc.getText().toString();

            // формируем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_PID, pid));
            params.add(new BasicNameValuePair(TAG_NAME, name));
            params.add(new BasicNameValuePair(TAG_PRICE, price));
            params.add(new BasicNameValuePair(TAG_DESCRIPTION, description));

            // отправляем измененные данные через http запрос
            JSONObject json = jsonParser.makeHttpRequest(url_update_product, "POST", params);

            // проверяем json success тег
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // продукт удачно обнавлён
                    Intent i = getIntent();
                    // отправляем результирующий код 100 чтобы сообщить об обновлении продукта
                    setResult(100, i);
                    finish();
                } else {
                    // продукт не обновлен
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * После окончания закрываем прогресс диалог
         **/
        protected void onPostExecute(String file_url) {
            // закрываем прогресс диалог
            pDialog.dismiss();
        }
    }

    /**
     * Фоновая асинхронная задача на удаление продукта
     **/
    class DeleteProduct extends AsyncTask<String, String, String> {

        /**
         * На начале показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditProductActivity.this);
            pDialog.setMessage("уДАЛЕМ ПРОДУКТ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Удаление продукта
         **/
        protected String doInBackground(String[] args) {

            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pid", pid));

                // получение продукта используя HTTP запрос
                JSONObject json = jsonParser.makeHttpRequest(url_delete_product, "POST", params);

                Log.d("Delete Product", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // Продукт удачно удален
                    Intent i = getIntent();
                    // отправляем результирующий код 100 для уведомления об удалении продукта
                    setResult(100, i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * После оконачния скрываем прогресс диалог
         **/
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();

        }

    }

}
