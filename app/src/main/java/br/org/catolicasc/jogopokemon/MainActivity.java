package br.org.catolicasc.jogopokemon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button button0;
    private Button button1;
    private Button button2;
    private Button button3;
    private ImageView tvPokemon;
    private TextView TotalAcertos;
    private TextView TotalErros;
    private TextView Score;
    private String salvarPkmn;
    private AlertDialog alerta;
    private AlertDialog.Builder builder;
    private AlertDialog.Builder confirmation;
    private int xJgd;
    private TextView txvCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvPokemon = findViewById(R.id.tvPokemon);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        TotalAcertos = findViewById(R.id.TotalAcertos);
        TotalErros = findViewById(R.id.TotalErros);
        Score = findViewById(R.id.txtScoreValue);
        builder = new AlertDialog.Builder(this);
        confirmation = new AlertDialog.Builder(this);
        xJgd = 1;
        txvCounter = findViewById(R.id.txvCounter);

        ContadorTempo();

        final DownloadDeDados downloadDeDados = new DownloadDeDados();
        downloadDeDados.execute("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");

        View.OnClickListener listenerButtons = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String nome = b.getText().toString();

                if(xJgd < 11) {
                    if (salvarPkmn.equals(nome)) {
                        int AcertoAtual = Integer.parseInt(TotalAcertos.getText().toString());
                        AcertoAtual += 1;
                        TotalAcertos.setText(String.valueOf(AcertoAtual));

                        int ScoreAtual = Integer.parseInt(Score.getText().toString());
                        ScoreAtual += 100;
                        Score.setText(String.valueOf(ScoreAtual));

                        builder.setTitle(nome + " diz:");
                        builder.setMessage("VOCÊ ACERTOU MEU NOME!!!");
                        alerta = builder.create();
                        alerta.show();

                        new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                alerta.cancel();
                            }
                        },
                        2000);

                    } else {
                        int ErroAtual = Integer.parseInt(TotalErros.getText().toString());
                        ErroAtual += 1;
                        TotalErros.setText(String.valueOf(ErroAtual));

                        int ScoreAtual = Integer.parseInt(Score.getText().toString());
                        ScoreAtual -= 100;
                        Score.setText(String.valueOf(ScoreAtual));

                        builder.setTitle("Pokemon diz:");
                        builder.setMessage("VOCÊ ERROU!!! MEU NOME É: " + salvarPkmn);
                        alerta = builder.create();
                        alerta.show();

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        alerta.cancel();
                                    }
                                },
                                2000);
                    }
                    if(xJgd == 10) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        FinalizarJogo();
                                    }
                                },
                        2500);
                    }
                    else {
                        xJgd++;
                        final DownloadDeDados downloadDeDados = new DownloadDeDados();
                        downloadDeDados.execute("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");
                    }
                }
            }
        };
        button0.setOnClickListener(listenerButtons);
        button1.setOnClickListener(listenerButtons);
        button2.setOnClickListener(listenerButtons);
        button3.setOnClickListener(listenerButtons);
    }

    private void ContadorTempo(){
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisecondsUntilDone) {
                txvCounter.setText(String.valueOf(millisecondsUntilDone / 1000));
            }

            public void onFinish() {
                FinalizarJogo();
                Log.i("Done!", "Coundown Timer Finished");
            }
        }.start();
    }

    private void FinalizarJogo(){
        confirmation.setTitle("O seu tempo acabou.Sua pontuação foi: " + Score.getText().toString())
        .setMessage("Você deseja recomeçar?")
        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                xJgd = 1;
                Score.setText(" ");
                TotalAcertos.setText(" ");
                TotalErros.setText(" ");
                dialog.cancel();
                final DownloadDeDados downloadDeDados = new DownloadDeDados();
                downloadDeDados.execute("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");
                ContadorTempo();
                dialog.dismiss();
            }
        })

        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        })
        .create().show();
    }

    private class DownloadDeDados extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: começa com o parâmetro: " + strings[0]);
            String jsonFeed = downloadJson(strings[0]);
            if (jsonFeed == null) {
                Log.e(TAG, "doInBackground: Erro baixando JSON");
            }
            return jsonFeed;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parâmetro é: " + s);
            JSONTokener jsonTokener = new JSONTokener(s);
            try {
                SetJSONData(jsonTokener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void SetJSONData(JSONTokener jsonTokener){
            try {
                JSONObject json = new JSONObject(jsonTokener);
                JSONArray jsonArray = json.getJSONArray("pokemon");
                Random random = new Random();
                int[] indice = new int[4];
                for(int i =0; i<4;i++){
                    indice[i] = random.nextInt(100);
                }

                SetTextInButtons(random, jsonArray, indice);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void SetTextInButtons(Random random, JSONArray jsonArray, int[] indice){
            try {
                button0.setText(jsonArray.getJSONObject(indice[0]).getString("name"));
                button1.setText(jsonArray.getJSONObject(indice[1]).getString("name"));
                button2.setText(jsonArray.getJSONObject(indice[2]).getString("name"));
                button3.setText(jsonArray.getJSONObject(indice[3]).getString("name"));
                ImageDownloader imageDownloader = new ImageDownloader();
                int pokemon = indice[random.nextInt(4)];
                salvarPkmn = jsonArray.getJSONObject(pokemon).getString("name");
                Bitmap imagem = imageDownloader.execute(jsonArray.getJSONObject(pokemon).getString("img").replace("http", "https")).get();
                tvPokemon.setImageBitmap(imagem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        private String downloadJson(String urlString) {
            StringBuilder json = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int resposta = connection.getResponseCode();
                Log.d(TAG, "downloadJson: O código de resposta foi: " + resposta);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                int charsLidos;
                char[] inputBuffer = new char[99999999];
                while (true) {
                    charsLidos = reader.read(inputBuffer);
                    if (charsLidos < 0) {
                        break;
                    }
                    if (charsLidos > 0) {
                        json.append(
                                String.copyValueOf(inputBuffer, 0, charsLidos));
                    }
                }
                reader.close();
                return json.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "DownloadJSON: URL é inválida " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadJson: Ocorreu um erro de IO ao baixar os dados: "
                        + e.getMessage());
            }
            return null;
        }
    }
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "ImageDownloader";

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                Log.d(TAG, "doInBackground: A imagem foi baixada com sucesso!"+ url);

                return bitmap;
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Erro ao baixar imagem " + e.getMessage());
            }
            return null;
        }
    }

}
