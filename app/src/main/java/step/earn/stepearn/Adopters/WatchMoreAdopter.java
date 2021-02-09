package step.earn.stepearn.Adopters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import step.earn.stepearn.R;


public class WatchMoreAdopter extends FirebaseRecyclerAdapter<WatchMoreItems, WatchMoreAdopter.WatchMoreItemsViewHolder> {


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public WatchMoreAdopter(@NonNull FirebaseRecyclerOptions<WatchMoreItems> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull WatchMoreItemsViewHolder holder, int position, @NonNull WatchMoreItems model) {
        int pos=position+1;
        holder.dayText.setText("Day\n"+pos);
        if (model.getDay()>=7)
            holder.statusImageView.setImageResource(R.drawable.checkmark);
        else
            holder.statusImageView.setImageResource(R.drawable.close);
    }

    @NonNull
    @Override
    public WatchMoreItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_extra_reward_update, parent, false);

        return new WatchMoreItemsViewHolder(view);
    }

    public static class WatchMoreItemsViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        ImageView statusImageView;
        public WatchMoreItemsViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText=itemView.findViewById(R.id.day);
            statusImageView=itemView.findViewById(R.id.status);
        }
    }
}
