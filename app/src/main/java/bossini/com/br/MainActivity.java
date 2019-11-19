package bossini.com.br;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView weatherRecyclerView;
    private WeatherAdapter adapter;
    private List <Weather> previsoes;

    // requestQueue para guardar as requisições
    private RequestQueue requestQueue;

    private EditText locationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        locationEditText = findViewById(R.id.locationEditText);
        setSupportActionBar(toolbar);

        // Volley, implementado para receber requisições e guardar cada uma em um objeto
        requestQueue = Volley.newRequestQueue(this);

        weatherRecyclerView = findViewById(R.id.weatherRecyclerView);
        previsoes = new ArrayList<>();
        adapter = new WeatherAdapter(this, previsoes);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        weatherRecyclerView.setAdapter(adapter);
        weatherRecyclerView.setLayoutManager(llm);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                String cidade = locationEditText.getText().toString();
                obtemPrevisoes(cidade);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void obtemPrevisoes (String cidade){
        String url = getString(R.string.web_service_url, cidade, getString(R.string.api_key));

        // criar uma requisição com retorno m JSON, vai colocar a saida em um objeto que vai facilitar a identificação
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                (resultado) ->{
            // processar JSON - ver estrutura do JSON, será um objeto dentro do resultado
                    // apaga todos resultados anteriores
                    previsoes.clear();
                    try {
                        JSONArray list = resultado.getJSONArray("list");
                        // percorre a lista toda e pega os valores a partir do no "list"
                        for (int i=0; i<list.length(); i++){

                            JSONObject iesimo = list.getJSONObject(i);
                            // pega cada nó de interesse
                            long dt = iesimo.getLong("dt");
                            // no JSON weather é um JSON object
                            JSONObject main = iesimo.getJSONObject("main");
                            double temp_min = main.getDouble("temp_min");
                            double temp_max = main.getDouble("temp_max");
                            double humidity = main.getDouble("humidity");
                            // descrição está dentro de um JSON array com um elemento só
                            JSONArray weather = iesimo.getJSONArray("weather");
                            String description = weather.getJSONObject(0).getString("description");
                            String icon = weather.getJSONObject(0).getString("icon");

                            Weather w = new Weather(dt,temp_min, temp_max, humidity, description, icon);
                            previsoes.add(w);

                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        ,(excecao) -> {
            Toast.makeText(this, getString(R.string.connect_error), Toast.LENGTH_SHORT).show();
            excecao.printStackTrace();
        });
        requestQueue.add(req);
    }
}

class WeatherViewHolder extends RecyclerView.ViewHolder{

    public ImageView conditionImageView;
    public TextView dayTextView;
    public TextView lowTextView;
    public TextView highTextView;
    public TextView humidityTextView;


    // Aponta uma vez para os componentes da raiz, os que precisarão ser chamados.
    public WeatherViewHolder (View raiz){
        super (raiz);
        this.conditionImageView = raiz.findViewById(R.id.conditionImageView);
        this.dayTextView = raiz.findViewById(R.id.lowTextView);
        this.highTextView = raiz.findViewById(R.id.highTextView);
        this.humidityTextView = raiz.findViewById(R.id.humidityTextView);
    }
}

// Criação do Adapter
// onCreateViewHolder = se foi chamado é pq não tem nenhuma árvore pra ser utilizada nesse pedaço. A árvore não existe
// Infla a árvore. Para inflar o layout precisa do Contexto.


// evita casting quando usa o genérico <WeatherViewHolder>
class WeatherAdapter extends RecyclerView.Adapter <WeatherViewHolder> {

    private Context context;

    public WeatherAdapter(Context context, List<Weather> previsoes) {
        this.context = context;
        this.previsoes = previsoes;
    }

    private List<Weather> previsoes;

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View raiz = inflater.inflate(R.layout.list_item, parent, false);
        return new WeatherViewHolder(raiz);
    }

    // a árvore já existia e precisa ser reutilizada, é rolada e será atualizada
    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        Weather w = previsoes.get(position);
        holder.lowTextView.setText(context.getString(R.string.low_temp,w.minTemp));
        holder.highTextView.setText(context.getString(R.string.high_temp,w.maxTemp));
        holder.humidityTextView.setText(context.getString(R.string.humidity,w.humidity));
        holder.dayTextView.setText(context.getString(R.string.day_description,w.dayOfWeek,w.description));
        Glide.with(context).load(w.iconURL).into(holder.conditionImageView);

    }

    @Override
    public int getItemCount() {
        return this.previsoes.size();
    }
}
