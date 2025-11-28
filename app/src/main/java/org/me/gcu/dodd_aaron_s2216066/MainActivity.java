/*
    Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 Aaron Dodd
// Student ID           s2216066
// Programme of Study   Bsc/Bsc (Hons) Software Development
//

package org.me.gcu.dodd_aaron_s2216066;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.me.gcu.dodd_aaron_s2216066.data.CurrencyRepository;
import org.me.gcu.dodd_aaron_s2216066.model.CurrencyRate;
import org.me.gcu.dodd_aaron_s2216066.ui.conversion.ConversionActivity;
import org.me.gcu.dodd_aaron_s2216066.ui.main.CurrencyRateAdapter;
import org.me.gcu.dodd_aaron_s2216066.viewmodel.CurrencyViewModel;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private TextView rawDataDisplay;
    private Button startButton;
    private String result;
    private String url1 = "";
    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";

    private TextView summaryTextView;
    private ListView ratesListView;
    private CurrencyRateAdapter adapter;

    private EditText searchEditText;
    private Button searchButton;
    private Button clearSearchButton;

    private CurrencyRepository currencyRepository;

    private CurrencyViewModel currencyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        summaryTextView = findViewById(R.id.summaryTextView);
        ratesListView = findViewById(R.id.ratesListView);
        adapter = new CurrencyRateAdapter(this);
        ratesListView.setAdapter(adapter);

        ratesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CurrencyRate selected = adapter.getItem(position);
                if (selected == null) {
                    return;
                }

                Intent intent = new Intent(MainActivity.this, ConversionActivity.class);

                intent.putExtra("code", selected.getCode());
                intent.putExtra("name", selected.getName());
                intent.putExtra("rate", selected.getRate());

                startActivity(intent);
            }
        });

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        clearSearchButton = findViewById(R.id.clearSearchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                currencyViewModel.setSearchQuery(query);
            }
        });

        clearSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
                currencyViewModel.setSearchQuery("");
            }
        });

        currencyRepository = new CurrencyRepository();

        currencyViewModel = new ViewModelProvider(this).get(CurrencyViewModel.class);

        currencyViewModel.getStatusText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String status) {
                List<CurrencyRate> list = currencyViewModel.getAllRates().getValue();
                int count = (list != null) ? list.size() : 0;
                String display = "Contains Fx Currency Exchange data\n"
                        + "Status: " + status + "\n"
                        + "Rates loaded: " + count;
                rawDataDisplay.setText(display);
            }
        });

        currencyViewModel.getFilteredRates().observe(this, new Observer<List<CurrencyRate>>() {
            @Override
            public void onChanged(List<CurrencyRate> currencyRates) {
                adapter.setData(currencyRates);
                updateSummary(currencyViewModel.getAllRates().getValue(),
                        currencyViewModel.getLastPubDate().getValue());
            }
        });

        currencyViewModel.getLastPubDate().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                updateSummary(currencyViewModel.getAllRates().getValue(), s);
            }
        });

        // More Code goes here
    }

    private void updateSummary(List<CurrencyRate> allRates, String pubDate) {
        if (allRates == null || allRates.isEmpty()) {
            summaryTextView.setText("Summary: no data loaded yet");
            return;
        }

        CurrencyRate usd = findByCode(allRates, "USD");
        CurrencyRate eur = findByCode(allRates, "EUR");
        CurrencyRate jpy = findByCode(allRates, "JPY");

        StringBuilder sb = new StringBuilder();
        sb.append("Summary (1 GBP = ...)\n");
        if (usd != null) {
            sb.append(String.format("USD: %.4f\n", usd.getRate()));
        }
        if (eur != null) {
            sb.append(String.format("EUR: %.4f\n", eur.getRate()));
        }
        if (jpy != null) {
            sb.append(String.format("JPY: %.4f\n", jpy.getRate()));
        }
        if (pubDate != null && !pubDate.isEmpty()) {
            sb.append("Last update: ").append(pubDate);
        }
        summaryTextView.setText(sb.toString());
    }

    private CurrencyRate findByCode(List<CurrencyRate> list, String code) {
        if (list == null || code == null) return null;
        for (CurrencyRate r : list) {
            if (r != null && code.equalsIgnoreCase(r.getCode())) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void onClick(View aview) {
        startProgress();
    }

    public void startProgress() {
        currencyViewModel.refreshRatesManually(urlSource);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                currencyViewModel.refreshRatesManually(urlSource);
                return true;

            case R.id.action_clear_search:
                if (searchEditText != null) {
                    searchEditText.setText("");
                }
                currencyViewModel.setSearchQuery("");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    private class Task implements Runnable {
//        private String url;
//        public Task(String aurl){
//            url = aurl;
//        }
//        @Override
//        public void run(){
//
//            Log.d("MyTask","in run (legacy Task - now unused by default)");
//
//            CurrencyRepository.RssResult rssResult = currencyRepository.downloadAndParse(url);
//            result = rssResult.cleanedXml;
//            List<CurrencyRate> parsedList = rssResult.rates;
//
//            try {
//                XmlPullParserFactory factory =
//                        XmlPullParserFactory.newInstance();
//                factory.setNamespaceAware(true);
//                XmlPullParser xpp = factory.newPullParser();
//                xpp.setInput(new StringReader(result));
//
//
//            } catch (XmlPullParserException e) {
//                Log.e("Parsing","EXCEPTION" + e);
//            }
//
//            String debugSummary = "";
//            if (parsedList != null && !parsedList.isEmpty()) {
//                CurrencyRate first = parsedList.get(0);
//                debugSummary = "\n\n--- PARSED FIRST ITEM (legacy Task) ---\n"
//                        + "Code: " + first.getCode() + "\n"
//                        + "Name: " + first.getName() + "\n"
//                        + "Country: " + first.getCountry() + "\n"
//                        + "Rate: " + first.getRate() + "\n"
//                        + "PubDate: " + first.getPubDate() + "\n";
//            } else {
//                debugSummary = "\n\n--- NO ITEMS PARSED (legacy Task) ---\n";
//            }
//
//            final String finalTextToShow = result + debugSummary;
//
//            MainActivity.this.runOnUiThread(new Runnable()
//            {
//                public void run() {
//                    Log.d("UI thread", "I am the UI thread");
//                    rawDataDisplay.setText(finalTextToShow);
//                }
//            });
//        }
//
//    }

}
