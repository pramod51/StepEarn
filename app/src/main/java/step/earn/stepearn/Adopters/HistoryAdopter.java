package step.earn.stepearn.Adopters;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import step.earn.stepearn.R;

public class HistoryAdopter extends FirebaseRecyclerAdapter<HistoryItems, HistoryAdopter.HistoryItemViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     */
    public HistoryAdopter(@NonNull FirebaseRecyclerOptions<HistoryItems> options) {
        super(options);
    }





    @Override
    protected void onBindViewHolder(@NonNull final HistoryItemViewHolder holder, int position, @NonNull final HistoryItems model) {
        holder.steps.setText(String.valueOf(model.getToday()));
        String[] months={"Jan","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        String[] days=getRef(position).toString().split("-");
        for (int i=0;i<days.length;i++)
        Log.v("Tag",""+i+days[i]);
        //String d=model.getDate(),m=""+d.charAt(5)+d.charAt(6);;
        int month=Integer.parseInt(days[3]);
        holder.date.setText(""+days[4]+"\n"+months[month]);

        String[] levels ={"Level 0","Level 1","Level 2","Level 3","Level 4"};
        String level=model.getLevel();
        if (model.getToday()>0)
            Picasso.get().load("abc").placeholder(R.drawable.check).into(holder.notZero);
        if (level!=null&&level.equals(levels[1]))
        holder.progressBar.setMax(3000);
        else if (level!=null&&level.equals(levels[2]))
            holder.progressBar.setMax(5000);
        else if (level!=null)
            holder.progressBar.setMax(10000);
        //holder.progressBar.setProgress(model.getToday());

        new CountDownTimer(model.getToday(),3){

            @Override
            public void onTick(long l) {

                holder.progressBar.setProgress( (model.getToday()- (int)l));
                //Log.v("tag",""+l);
                //resend.setEnabled(false);
            }

            @Override
            public void onFinish() {
                //resend.setText("Resend");
                //resend.setEnabled(true);
                holder.progressBar.setProgress(model.getToday());

            }
        }.start();





    }

    @NonNull
    @Override
    public HistoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_items, parent, false);

        return new HistoryItemViewHolder(view);
    }

    public static class HistoryItemViewHolder extends RecyclerView.ViewHolder {
        TextView steps;
        ImageView notZero;
        TextView date;
        ProgressBar progressBar;
        public HistoryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            steps=itemView.findViewById(R.id.steps);
            notZero=itemView.findViewById(R.id.not_zero);
            date=itemView.findViewById(R.id.date);
            progressBar=itemView.findViewById(R.id.progress_bar);
        }
    }


}
