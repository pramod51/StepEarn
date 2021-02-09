package step.earn.stepearn.Adopters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import step.earn.stepearn.R;


public class AdaptorClass extends FirebaseRecyclerAdapter<AdopterItems, AdaptorClass.AdapterViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AdaptorClass(@NonNull FirebaseRecyclerOptions<AdopterItems> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterViewHolder holder, int position, @NonNull AdopterItems model) {
        holder.textView.setText(model.getLines());
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reward_items_detaails, parent, false);
        return new AdapterViewHolder(view);
    }

    public class AdapterViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.text);

        }
    }


}