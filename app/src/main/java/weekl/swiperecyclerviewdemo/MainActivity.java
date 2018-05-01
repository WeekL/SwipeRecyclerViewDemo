package weekl.swiperecyclerviewdemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import weekl.swiperecycler.SwipeRecyclerView;

public class MainActivity extends AppCompatActivity {
    private SwipeRecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add("第" + (i + 1) + "个");
        }
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DemoAdapter(this, data));
    }

    class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.ViewHolder> {
        private static final String TAG = "DemoAdapter";
        private Context mContext;
        private List<String> mDatas;

        public DemoAdapter(Context context, List<String> datas) {
            mContext = context;
            mDatas = datas;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_demo, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "点击了item: " + position);
                }
            });
            holder.msg.setText(mDatas.get(position));
            holder.msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Click" + (position + 1), Toast.LENGTH_SHORT).show();
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Delete" + (position + 1), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDatas == null ? 0 : mDatas.size();
        }

        class ViewHolder extends SwipeRecyclerView.ViewHolder {
            TextView msg;
            TextView top;
            TextView delete;

            public ViewHolder(View itemView) {
                super(itemView);
                msg = itemView.findViewById(R.id.item_msg);
                top = itemView.findViewById(R.id.item_top);
                delete = itemView.findViewById(R.id.item_delete);
            }

            @Override
            public int bindContentLayout() {
                return R.id.item_content;
            }

            @Override
            public int bindMenuLayout() {
                return R.id.item_menu;
            }
        }
    }
}
