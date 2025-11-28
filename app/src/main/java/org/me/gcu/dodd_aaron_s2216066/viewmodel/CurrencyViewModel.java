package org.me.gcu.dodd_aaron_s2216066.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.me.gcu.dodd_aaron_s2216066.data.CurrencyRepository;
import org.me.gcu.dodd_aaron_s2216066.model.CurrencyRate;

import java.util.ArrayList;
import java.util.List;

public class CurrencyViewModel extends ViewModel {

    private static final String TAG = "CurrencyViewModel";

    private static final long AUTO_UPDATE_INTERVAL_MS = 60_000L; // demo

    private final CurrencyRepository repository;

    private final MutableLiveData<List<CurrencyRate>> allRates = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<CurrencyRate>> filteredRates = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> statusText = new MutableLiveData<>("Press button to load data");
    private final MutableLiveData<String> lastPubDate = new MutableLiveData<>("");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Auto update triggered");
            refreshRatesInternal(false);
            // Schedule next auto-update
            handler.postDelayed(this, AUTO_UPDATE_INTERVAL_MS);
        }
    };

    private String currentSearchQuery = "";

    public CurrencyViewModel() {
        repository = new CurrencyRepository();
        handler.postDelayed(autoUpdateRunnable, AUTO_UPDATE_INTERVAL_MS);
    }

    public LiveData<List<CurrencyRate>> getAllRates() {
        return allRates;
    }

    public LiveData<List<CurrencyRate>> getFilteredRates() {
        return filteredRates;
    }

    public LiveData<String> getStatusText() {
        return statusText;
    }

    public LiveData<String> getLastPubDate() {
        return lastPubDate;
    }

    public void refreshRatesManually(String urlSource) {
        refreshRatesInternal(true, urlSource);
    }

    private String cachedUrl = null;

    private void refreshRatesInternal(boolean manual) {
        refreshRatesInternal(manual, cachedUrl);
    }

    private void refreshRatesInternal(boolean manual, String urlSource) {
        if (urlSource == null || urlSource.isEmpty()) {
            statusText.postValue("ERROR: URL not set");
            return;
        }
        cachedUrl = urlSource;

        if (manual) {
            statusText.postValue("Refreshing rates...");
        } else {
            statusText.postValue("Auto-updating rates...");
        }

        new Thread(() -> {
            Log.d(TAG, "refreshRatesInternal: starting background fetch");
            CurrencyRepository.RssResult rssResult = repository.downloadAndParse(urlSource);

            if (rssResult.cleanedXml == null || rssResult.cleanedXml.isEmpty()) {
                statusText.postValue("ERROR: failed to load data");
                return;
            }

            List<CurrencyRate> rates = rssResult.rates != null ? rssResult.rates : new ArrayList<>();
            allRates.postValue(rates);

            String pubDate = "";
            if (!rates.isEmpty()) {
                pubDate = rates.get(0).getPubDate();
            }
            lastPubDate.postValue(pubDate);

            applyFilterInternal(currentSearchQuery, rates);

            if (manual) {
                statusText.postValue("Loaded " + rates.size() + " rates (manual refresh)");
            } else {
                statusText.postValue("Loaded " + rates.size() + " rates (auto-update)");
            }

        }).start();
    }

    public void setSearchQuery(String query) {
        currentSearchQuery = query != null ? query.trim() : "";
        List<CurrencyRate> base = allRates.getValue();
        if (base == null) {
            base = new ArrayList<>();
        }
        applyFilterInternal(currentSearchQuery, base);
    }

    private void applyFilterInternal(String query, List<CurrencyRate> baseList) {
        if (query == null) {
            query = "";
        }
        String q = query.toLowerCase();

        if (q.isEmpty()) {
            filteredRates.postValue(new ArrayList<>(baseList));
            return;
        }

        List<CurrencyRate> out = new ArrayList<>();
        for (CurrencyRate rate : baseList) {
            if (rate == null) continue;
            String code = rate.getCode() != null ? rate.getCode().toLowerCase() : "";
            String name = rate.getName() != null ? rate.getName().toLowerCase() : "";
            String country = rate.getCountry() != null ? rate.getCountry().toLowerCase() : "";

            if (code.contains(q) || name.contains(q) || country.contains(q)) {
                out.add(rate);
            }
        }
        filteredRates.postValue(out);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacks(autoUpdateRunnable);
    }
}
