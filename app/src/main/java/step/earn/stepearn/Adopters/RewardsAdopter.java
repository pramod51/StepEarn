package step.earn.stepearn.Adopters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import step.earn.stepearn.R;
import step.earn.stepearn.RewardDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

public class RewardsAdopter extends FirebaseRecyclerAdapter<RewardsItems, RewardsAdopter.rewardsItemViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    public RewardsAdopter(@NonNull FirebaseRecyclerOptions<RewardsItems> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final rewardsItemViewHolder holder, int position, @NonNull final RewardsItems model) {
        final int pos=position+1;
        holder.tittel.setText(model.getTittle());
        holder.claim.setText(model.getButtonText());
        //Log.v("Tag",model.getTittle());
        //Log.v("Tag",model.getTittle()+"okk not showing");
        Picasso.get().load(model.getImageUrl()).into(holder.rewardImage);
        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),RewardDetails.class);
                intent.putExtra("xyz",""+pos);
                holder.itemView.getContext().startActivity(intent);
            }
        });


    }


    @NonNull
    @Override
    public rewardsItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_rewards, parent, false);

        return new rewardsItemViewHolder(view);

    }

    static class rewardsItemViewHolder extends RecyclerView.ViewHolder {

        TextView tittel;
        ImageView rewardImage;
        TextView claim;
        Button details;

        public rewardsItemViewHolder(@NonNull final View itemView) {
            super(itemView);
            tittel=itemView.findViewById(R.id.tittle);
            rewardImage=itemView.findViewById(R.id.reward_image);
            claim=itemView.findViewById(R.id.claim);
            details=itemView.findViewById(R.id.details);

        }
    }
}
