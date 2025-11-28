package org.me.gcu.dodd_aaron_s2216066.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.me.gcu.dodd_aaron_s2216066.R;
import org.me.gcu.dodd_aaron_s2216066.model.CurrencyRate;

import java.util.ArrayList;
import java.util.List;

public class CurrencyRateAdapter extends ArrayAdapter<CurrencyRate> {

    private List<CurrencyRate> rates;
    private Context context;

    public CurrencyRateAdapter(Context context) {
        super(context, R.layout.list_item_currency_rate, new ArrayList<>());
        this.context = context;
        this.rates = new ArrayList<>();
    }

    public void setData(List<CurrencyRate> newRates) {
        this.rates = (newRates != null) ? newRates : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rates.size();
    }

    @Override
    public CurrencyRate getItem(int position) {
        return rates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_currency_rate, parent, false);
            holder = new ViewHolder();
            holder.flagImageView = convertView.findViewById(R.id.flagImageView);
            holder.codeTextView = convertView.findViewById(R.id.codeTextView);
            holder.nameTextView = convertView.findViewById(R.id.nameTextView);
            holder.countryTextView = convertView.findViewById(R.id.countryTextView);
            holder.rateTextView = convertView.findViewById(R.id.rateTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CurrencyRate rate = rates.get(position);

        int resId = 0;
        if (rate.getCode() != null) {
            String code = rate.getCode().toLowerCase();
            if ("try".equals(code)) {
                resId = R.drawable.try_flag;
            } else {
                resId = context.getResources()
                        .getIdentifier(code, "drawable", context.getPackageName());
            }
        }

        if (resId != 0) {
            holder.flagImageView.setImageResource(resId);
        } else {
            holder.flagImageView.setImageResource(android.R.drawable.ic_dialog_map);
        }

        holder.codeTextView.setText(rate.getCode());
        holder.nameTextView.setText(rate.getName());
        holder.countryTextView.setText(rate.getCountry());
        holder.rateTextView.setText(String.format("1 GBP = %.4f", rate.getRate()));

        int backgroundColor = getColorForRate(rate.getRate());
        convertView.setBackgroundColor(backgroundColor);

        return convertView;
    }

    private int getColorForRate(double rate) {
        if (rate < 1.0) {
            return ContextCompat.getColor(context, R.color.color_red);
        } else if (rate < 5.0) {
            return ContextCompat.getColor(context, R.color.color_orange);
        } else if (rate < 10.0) {
            return ContextCompat.getColor(context, R.color.color_yellow);
        } else {
            return ContextCompat.getColor(context, R.color.color_green);
        }
    }

    private static class ViewHolder {
        ImageView flagImageView;
        TextView codeTextView;
        TextView nameTextView;
        TextView countryTextView;
        TextView rateTextView;
    }
}